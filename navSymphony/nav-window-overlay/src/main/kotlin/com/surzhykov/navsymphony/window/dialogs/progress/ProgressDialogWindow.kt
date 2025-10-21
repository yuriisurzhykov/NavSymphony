package com.surzhykov.navsymphony.window.dialogs.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.surzhykov.navsymphony.core.presentation.StringResolver
import com.surzhykov.navsymphony.window.R
import com.surzhykov.navsymphony.window.core.AbstractOverlayWindow
import com.surzhykov.navsymphony.window.core.LocalOverlayManager
import com.surzhykov.navsymphony.window.dialogs.CenteredDialogContent
import com.surzhykov.navsymphony.window.dialogs.DialogScope
import com.surzhykov.navsymphony.window.overlay.AnchorViewInfo
import com.surzhykov.navsymphony.window.overlay.HorizontalReferencePoint
import com.surzhykov.navsymphony.window.overlay.VerticalReferencePoint
import com.surzhykov.navsymphony.window.overlay.to

// Private default value for onDismiss callback to be able to compare onDismiss property of
// PrimaryDialog in actual code by reference.
private val EmptyOnDismiss = {}

/**
 * Overlay window representing a progress dialog with animated dots and a message.
 *
 * This window is centered in the screen. If [dismissEnabled] is true and [onDismiss] is non-default,
 * a cancel button is displayed in the top-right corner.
 *
 * @property messageResolver Message text resolver shown below the animation.
 * @property dismissEnabled Indicates whether user can dismiss the dialog.
 * @property onDismiss Callback invoked when user taps the cancel button.
 */
@Immutable
data class ProgressDialogWindow(
    private val messageResolver: StringResolver,
    private val dismissEnabled: Boolean,
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
     * Renders the content of the progress dialog inside an overlay.
     *
     * Displays cancel button (if enabled), animated dots, and a centered message.
     *
     * @param modifier Modifier applied to the root container.
     */
    @Composable
    override fun BoxScope.RenderContent(modifier: Modifier) {
        val navigationController by rememberUpdatedState(LocalOverlayManager.current)
        val dialogScope = remember { DialogScope.Base() }
        dialogScope.CenteredDialogContent(modifier = Modifier) {
            if (dismissEnabled && onDismiss !== EmptyOnDismiss) {
                IconButton(
                    modifier = Modifier.align(Alignment.TopEnd),
                    onClick = {
                        onDismiss.invoke()
                        navigationController.dismissOverlay(id)
                    },
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_button_dialog_cancel),
                        contentDescription = stringResource(R.string.label_button_cancel)
                    )
                }
            }
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                AnimatedDotsIndicator(
                    dotColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(12.dp)
                )
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    text = messageResolver.asString(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    /* Override equals and hashCode to include properties that are not part of this class but
    * are part of the superclass. Super class properties must be included in the equals and
    * hashCode implementations. To avoid unexpected behavior with the comparing of two different
    * instances of the class.*/
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProgressDialogWindow) return false
        if (!super.equals(other)) return false

        if (dismissEnabled != other.dismissEnabled) return false
        if (messageResolver != other.messageResolver) return false
        if (onDismiss != other.onDismiss) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + dismissEnabled.hashCode()
        result = 31 * result + messageResolver.hashCode()
        result = 31 * result + onDismiss.hashCode()
        return result
    }
}