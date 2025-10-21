package com.surzhykov.navsymphony.window.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.surzhykov.navsymphony.core.presentation.DrawableResolver
import com.surzhykov.navsymphony.core.presentation.StringResolver
import com.surzhykov.navsymphony.window.core.AbstractOverlayWindow
import com.surzhykov.navsymphony.window.core.LocalOverlayManager
import com.surzhykov.navsymphony.window.dialogs.DialogButton.Companion.rememberDialogButtonOk
import com.surzhykov.navsymphony.window.overlay.AnchorViewInfo
import com.surzhykov.navsymphony.window.overlay.HorizontalReferencePoint
import com.surzhykov.navsymphony.window.overlay.VerticalReferencePoint
import com.surzhykov.navsymphony.window.overlay.to
import kotlinx.collections.immutable.persistentListOf

private val EmptyOnDismiss = {}

/**
 * System-controlled overlay dialog displaying a message with a single "OK" button.
 *
 * This dialog is intended for one-way, non-interactive system communication to the user.
 * It is created and shown automatically (e.g. from within an `IntentValidationRule` or a
 * navigation handler), without requiring Composable state control or lifecycle awareness.
 *
 * The dialog is dismissed automatically when the user taps the "OK" button, using
 * [LocalOverlayManager]. No explicit `onDismiss` callback is required.
 *
 * @property iconResolver Resolves the icon displayed in the top-left corner.
 * @property titleResolver Resolves the dialog title string.
 * @property messageResolver Optional body message shown below the title.
 */
@Immutable
data class PrimaryMessageDialog(
    private val iconResolver: DrawableResolver,
    private val titleResolver: StringResolver,
    private val messageResolver: StringResolver?,
    override val onDismiss: () -> Unit = EmptyOnDismiss,
) : AbstractOverlayWindow(
    anchorViewInfo = AnchorViewInfo.Base(
        DpSize.Zero,
        DpOffset.Zero,
        HorizontalReferencePoint.Center to HorizontalReferencePoint.Center,
        VerticalReferencePoint.Center to VerticalReferencePoint.Center
    ),
    onDismiss = onDismiss
) {

    /**
     * Renders the content of the dialog inside an overlay window.
     *
     * The layout consists of an icon, a scrollable title/message block, and a single
     * "OK" button. When tapped, the button automatically dismisses the dialog via
     * [LocalOverlayManager.dismissOverlay].
     *
     * @param modifier Modifier applied to the outer container.
     */
    @Composable
    override fun BoxScope.RenderContent(modifier: Modifier) {
        val navigationController = LocalOverlayManager.current
        val okButton = rememberDialogButtonOk {
            navigationController.dismissOverlay(id)
            if (onDismiss != EmptyOnDismiss) onDismiss.invoke()
        }
        val dialogScope = remember { DialogScope.Base() }
        dialogScope.CenteredDialogContent(modifier = Modifier) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = iconResolver.resolve(),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.Top)
                        .padding(16.dp)
                        .size(48.dp),
                    tint = Color.Unspecified
                )
                Column(
                    modifier = Modifier
                        .padding(end = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    DialogTextContent(
                        titleResolver = titleResolver,
                        messageResolver = messageResolver,
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(
                                horizontal = 16.dp,
                                vertical = 8.dp
                            ),
                    )
                    DialogButtonsContent(
                        buttons = remember { persistentListOf(okButton) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    )
                }
            }
        }
    }

    /* Override equals and hashCode to include properties that are not part of this class but
    * are part of the superclass. Super class properties must be included in the equals and
    * hashCode implementations. To avoid unexpected behavior with the comparing of two different
    * instances of the class.*/
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PrimaryMessageDialog) return false
        if (!super.equals(other)) return false

        if (iconResolver != other.iconResolver) return false
        if (titleResolver != other.titleResolver) return false
        if (messageResolver != other.messageResolver) return false
        if (onDismiss != other.onDismiss) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + iconResolver.hashCode()
        result = 31 * result + titleResolver.hashCode()
        result = 31 * result + (messageResolver?.hashCode() ?: 0)
        result = 31 * result + onDismiss.hashCode()
        return result
    }
}