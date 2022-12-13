package network.zenon.android.embedded

class Validations {
    companion object {
        fun tokenName(value: String?): String? {
            return if (value != null) {
                if (value.isEmpty()) {
                    "Token name can't be empty"
                } else if (!TOKEN_NAME_REG_EXP.containsMatchIn(value)) {
                    "Token name must contain only alphanumeric characters"
                } else if (value.length > TOKEN_NAME_MAX_LENGTH) {
                    "Token name must have maximum $TOKEN_NAME_MAX_LENGTH characters"
                } else {
                    null
                }
            } else {
                "Value is null"
            }
        }

        fun tokenSymbol(value: String?): String? {
            return if (value != null) {
                if (value.isEmpty()) {
                    "Token symbol can't be empty"
                } else if (!TOKEN_SYMBOL_REG_EXP.containsMatchIn(value)) {
                    "Token symbol must match pattern: ${TOKEN_SYMBOL_REG_EXP.pattern}"
                } else if (value.length > TOKEN_SYMBOL_MAX_LENGTH) {
                    "Token symbol must have maximum $TOKEN_SYMBOL_MAX_LENGTH characters"
                } else if (TOKEN_SYMBOL_EXCEPTIONS.contains(value)) {
                    "Token symbol must not be one of the following: ${TOKEN_SYMBOL_EXCEPTIONS.joinToString(", ")}"
                } else {
                    null
                }
            } else {
                "Value is null"
            }
        }

        fun tokenDomain(value: String?): String? {
            return if (value != null) {
                if (value.isEmpty()) {
                    "Token domain can't be empty"
                } else if (!TOKEN_DOMAIN_REG_EXP.containsMatchIn(value)) {
                    "Domain is not valid"
                } else {
                    null
                }
            } else {
                "Value is null"
            }
        }

        fun pillarName(value: String?): String? {
            return if (value != null) {
                if (value.isEmpty()) {
                    "Pillar name can't be empty"
                } else if (!PILLAR_NAME_REG_EXP.containsMatchIn(value)) {
                    "Pillar name must match pattern : ${PILLAR_NAME_REG_EXP.pattern}"
                } else if (value.length > PILLAR_NAME_MAX_LENGTH) {
                    "Pillar name must have maximum $PILLAR_NAME_MAX_LENGTH characters"
                } else {
                    null
                }
            } else {
                "Value is null"
            }
        }

        fun projectName(value: String?): String? {
            return if (value != null) {
                if (value.isEmpty()) {
                    "Project name can't be empty"
                } else if (value.length > PROJECT_NAME_MAX_LENGTH) {
                    "Project name must have maximum $PROJECT_NAME_MAX_LENGTH characters"
                } else {
                    null
                }
            } else {
                "Value is null"
            }
        }

        fun projectDescription(value: String?): String? {
            return if (value != null) {
                if (value.isEmpty()) {
                    "Project description can't be empty"
                } else if (value.length > PROJECT_DESCRIPTION_MAX_LENGTH) {
                    "Project description must have maximum $PROJECT_DESCRIPTION_MAX_LENGTH characters"
                } else {
                    null
                }
            } else {
                "Value is null"
            }
        }
    }
}