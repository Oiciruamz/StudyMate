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
    BREAK
}

/**
 * Funciones de extensión para formatear el texto de la respuesta de IA
 */
fun String.toResponseSections(): List<ResponseSection> {
    val sections = mutableListOf<ResponseSection>()
    val lines = this.split("\n")
    
    var currentType: ResponseSectionType? = null
    var currentContent = StringBuilder()
    
    for (line in lines) {
        val trimmedLine = line.trim()
        
        if (trimmedLine.isEmpty()) {
            // Finalizar sección actual si hay contenido
            if (currentContent.isNotEmpty() && currentType != null) {
                sections.add(ResponseSection(currentType, currentContent.toString().trim()))
                currentContent.clear()
                sections.add(ResponseSection(ResponseSectionType.BREAK, ""))
            }
            continue
        }
        
        // Determinar el tipo de línea
        val newType = when {
            trimmedLine.startsWith("# ") -> ResponseSectionType.TITLE
            trimmedLine.startsWith("## ") -> ResponseSectionType.SUBTITLE
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
            ResponseSectionType.SUBTITLE -> trimmedLine.substringAfter("## ")
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