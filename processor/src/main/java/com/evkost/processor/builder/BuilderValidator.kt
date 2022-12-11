package com.evkost.processor.builder

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.*
import com.evkost.processor.hasAllDefaults
import com.evkost.processor.hasModifier
import com.evkost.processor.validator.ValidateVisitor
import com.evkost.processor.validator.Validated

internal class BuilderValidator(private val logger: KSPLogger) : ValidateVisitor<Unit>(logger) {
    override fun defaultHandler(node: KSNode, data: Unit): Validated<ValidateVisitor<Unit>> {
        logger.error("Builder can be created only for classes", node)

        return super.defaultHandler(node, data)
    }

    override fun visitClassDeclaration(
        classDeclaration: KSClassDeclaration, data: Unit
    ): Validated<BuilderValidator> = validate(classDeclaration) {
        assertNot { isAbstract() } error "Cannot create builder for abstract class"

        assertNot { isCompanionObject } error "Cannot create builder for companion object"

        assertNot { hasModifier(Modifier.PRIVATE) } error "Cannot create builder for private class"

        assertNot { hasModifier(Modifier.INTERNAL) } error "Cannot create builder for internal class (will be added in future)" //TODO apply internal in future

        assertNot { hasModifier(Modifier.SEALED) } error "Cannot create builder for sealed class (will be added in future)" //TODO apply sealed classes in future

        assertNotNull { primaryConstructor } error "Cannot create builder for class, that hasn't primary constructor"

        assertNot { primaryConstructor?.hasModifier(Modifier.PRIVATE) ?: false } error "Cannot create builder for class, that has private primary constructor"

        assert { classKind == ClassKind.CLASS } error "Builder can be created only for common classes"

        val properties = element.getDeclaredProperties()
        val havePrivateProperty = element.primaryConstructor!!.parameters.any { parameter ->
            properties.find { it.simpleName.asString() == parameter.name?.asString() }?.hasModifier(Modifier.PRIVATE) == true
        }

        val haveProtectedProperty = element.primaryConstructor!!.parameters.any { parameter ->
            properties.find { it.simpleName.asString() == parameter.name?.asString() }?.hasModifier(Modifier.PROTECTED) == true
        }

        assertNot { havePrivateProperty } error "Cannot create builder for class, that has private property in primary constructor"

        assertNot { haveProtectedProperty } error "Cannot create builder for class, that has protected property in primary constructor"

        check { hasAllDefaults ?: false } warn "Builder for ${element.simpleName.asString()} class " +
                "will have all nullable properties with null values by default, because not all properties have a default value"
    }
}

