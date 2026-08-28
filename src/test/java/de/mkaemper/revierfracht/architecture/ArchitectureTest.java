package de.mkaemper.revierfracht.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import java.time.Instant;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Modules are the first-level packages under {@link #BASE_PACKAGE}, each following the
 * {@code <module>.api}, {@code <module>.application}, {@code <module>.domain},
 * {@code <module>.infrastructure} convention. {@code api} is the only package a module
 * may expose to other modules; {@code application}, {@code domain} and
 * {@code infrastructure} are internal. Two rules allow an empty {@code should} because
 * the packages are currently scaffolding without classes; they start enforcing the
 * moment real code lands in them.
 */
@AnalyzeClasses(packages = ArchitectureTest.BASE_PACKAGE)
class ArchitectureTest {

	static final String BASE_PACKAGE = "de.mkaemper.revierfracht";

	private static final Pattern MODULE_INTERNAL_PACKAGE = Pattern.compile(
			"^" + Pattern.quote(BASE_PACKAGE) + "\\.([^.]+)\\.(domain|application|infrastructure)(\\..+)?$");

	private static final Pattern MODULE_PACKAGE = Pattern.compile(
			"^" + Pattern.quote(BASE_PACKAGE) + "\\.([^.]+)(\\..+)?$");

	@ArchTest
	static final ArchRule domain_must_not_depend_on_spring = noClasses()
			.that().resideInAPackage("..domain..")
			.should().dependOnClassesThat().resideInAnyPackage("org.springframework..", "jakarta.persistence..")
			.because("domain code must stay framework-free")
			.allowEmptyShould(true);

	@ArchTest
	static final ArchRule domain_must_not_call_instant_now = noClasses()
			.that().resideInAPackage("..domain..")
			.should().callMethod(Instant.class, "now")
			.because("domain code must obtain time from the injected Clock bean via Instant.now(clock), not the zone-default Instant.now()")
			.allowEmptyShould(true);

	@ArchTest
	static final ArchRule api_must_not_depend_on_infrastructure = noClasses()
			.that().resideInAPackage("..api..")
			.should().dependOnClassesThat().resideInAPackage("..infrastructure..")
			.because("api is a module's public contract and must not leak infrastructure details")
			.allowEmptyShould(true);

	@ArchTest
	static final ArchRule modules_must_not_reach_into_other_modules_internals = classes()
			.should(new ArchCondition<JavaClass>("only access application/domain/infrastructure of their own module") {
				@Override
				public void check(JavaClass clazz, ConditionEvents events) {
					Optional<String> sourceModule = moduleOf(clazz.getPackageName());
					clazz.getDirectDependenciesFromSelf().forEach(dependency -> {
						String targetPackage = dependency.getTargetClass().getPackageName();
						Matcher internalMatcher = MODULE_INTERNAL_PACKAGE.matcher(targetPackage);
						if (!internalMatcher.matches()) {
							return;
						}
						String targetModule = internalMatcher.group(1);
						if (sourceModule.filter(targetModule::equals).isPresent()) {
							return;
						}
						events.add(SimpleConditionEvent.violated(clazz, String.format(
								"%s reaches into %s.%s of module '%s' from outside that module",
								clazz.getFullName(), targetModule, internalMatcher.group(2), targetModule)));
					});
				}
			})
			.because("a module's application, domain and infrastructure are internal; other modules may only use its api");

	private static Optional<String> moduleOf(String packageName) {
		Matcher matcher = MODULE_PACKAGE.matcher(packageName);
		return matcher.matches() ? Optional.of(matcher.group(1)) : Optional.empty();
	}
}
