package com.example.studym8.data.model

data class ResponseSection(
    val type: ResponseSectionType,
    val content: String
)

enum class ResponseSectionType {
    TITLE,
    SUBTITLE,
    PARAGRAPH,
    INFO_ITEM,
    BREAK,
    CODE_START,
    CODE_LINE,
    CODE_END,
    BOLD
}

/**
 * Funciones de extensión para formatear el texto de la respuesta de IA
 */
fun String.toResponseSections(): List<ResponseSection> {
    val sections = mutableListOf<ResponseSection>()
    val lines = this.split("\n")
    
    var currentType: ResponseSectionType? = null
    var currentContent = StringBuilder()
    var inCodeBlock = false
    
    for (line in lines) {
        val trimmedLine = line.trim()
        
        // Si es una línea vacía
        if (trimmedLine.isEmpty()) {
            // Finalizar sección actual si hay contenido
            if (currentContent.isNotEmpty() && currentType != null) {
                sections.add(ResponseSection(currentType, currentContent.toString().trim()))
                currentContent.clear()
                sections.add(ResponseSection(ResponseSectionType.BREAK, ""))
            }
            continue
        }
        
        // Manejo de bloques de código
        if (trimmedLine == "```") {
            if (currentContent.isNotEmpty() && currentType != null) {
                sections.add(ResponseSection(currentType, currentContent.toString().trim()))
                currentContent.clear()
            }
            
            inCodeBlock = !inCodeBlock
            if (inCodeBlock) {
                sections.add(ResponseSection(ResponseSectionType.CODE_START, ""))
            } else {
                sections.add(ResponseSection(ResponseSectionType.CODE_END, ""))
            }
            continue
        }
        
        // Código dentro de un bloque de código
        if (inCodeBlock) {
            sections.add(ResponseSection(ResponseSectionType.CODE_LINE, trimmedLine))
            continue
        }
        
        // Procesar negritas en el texto
        if (trimmedLine.contains("**")) {
            val parts = processFormattedText(trimmedLine)
            parts.forEach { sections.add(it) }
            continue
        }
        
        // Determinar el tipo de línea
        val newType = when {
            trimmedLine.startsWith("# ") -> ResponseSectionType.TITLE
            trimmedLine.startsWith("## ") -> ResponseSectionType.SUBTITLE
            trimmedLine.startsWith("##Sesión") -> ResponseSectionType.SUBTITLE
            trimmedLine.startsWith("- ") || trimmedLine.startsWith("* ") -> ResponseSectionType.INFO_ITEM
            else -> ResponseSectionType.PARAGRAPH
        }
        
        // Si cambia el tipo, finalizar la sección actual y comenzar una nueva
        if (currentType != null && currentType != newType && currentContent.isNotEmpty()) {
            sections.add(ResponseSection(currentType, currentContent.toString().trim()))
            currentContent.clear()
        }
        
        currentType = newType
        
        // Agregar contenido sin los marcadores de formato
        val contentToAdd = when (newType) {
            ResponseSectionType.TITLE -> trimmedLine.substringAfter("# ")
            ResponseSectionType.SUBTITLE -> {
                // Para sesiones, extraer "Sesión X" y el resto del título
                if (trimmedLine.contains("Sesión")) {
                    // Manejar casos donde puede estar como ##Sesión o ## Sesión
                    val sessionText = if (trimmedLine.contains("## Sesión")) {
                        trimmedLine.substringAfter("## ")
                    } else if (trimmedLine.contains("##Sesión")) {
                        trimmedLine.substringAfter("##")
                    } else {
                        trimmedLine.substringAfter("## ")
                    }
                    
                    val sessionPart = if (sessionText.contains(":")) {
                        "Sesión " + sessionText.substringAfter("Sesión ").substringBefore(":")
                    } else {
                        sessionText
                    }
                    
                    val titlePart = if (sessionText.contains(":")) {
                        sessionText.substringAfter(":")
                    } else {
                        ""
                    }
                    
                    if (titlePart.isNotEmpty()) "$sessionPart:$titlePart" else sessionPart
                } else {
                    trimmedLine.substringAfter("## ")
                }
            }
            ResponseSectionType.INFO_ITEM -> trimmedLine.substringAfter("- ").substringAfter("* ")
            else -> trimmedLine
        }
        
        if (currentContent.isNotEmpty()) {
            currentContent.append(" ")
        }
        currentContent.append(contentToAdd)
    }
    
    // Agregar la última sección si hay contenido pendiente
    if (currentContent.isNotEmpty() && currentType != null) {
        sections.add(ResponseSection(currentType, currentContent.toString().trim()))
    }
    
    return sections
}

/**
 * Procesa texto que contiene marcadores de formato como negritas
 */
private fun processFormattedText(text: String): List<ResponseSection> {
    val sections = mutableListOf<ResponseSection>()
    val parts = text.split("**")
    
    for (i in parts.indices) {
        if (parts[i].isNotEmpty()) {
            // Los índices impares (1, 3, 5...) corresponden al texto entre ** (negritas)
            val type = if (i % 2 == 1) ResponseSectionType.BOLD else ResponseSectionType.PARAGRAPH
            sections.add(ResponseSection(type, parts[i]))
        }
    }
    
    return sections
} 