// app/src/main/java/com/rdinfo2/ui/components/gesture/OverlayState.kt
package com.rdinfo2.ui.components.gesture

/**
 * Shared enum für alle Overlay-Zustände
 * Verhindert Redeclaration-Fehler zwischen verschiedenen Gesture-Komponenten
 */
enum class OverlayState {
    HIDDEN,
    PATIENT_DATA,  // Oben nach unten wischen
    SAMPLER,       // Links nach rechts wischen (später)
    XABCDE         // Rechts nach links wischen (später)
}