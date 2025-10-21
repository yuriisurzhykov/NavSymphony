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
import com.surzhykov.navsymphony.window.core.OverlayManager
import com.surzhykov.navsymphony.window.dialogs.DialogButton.Companion.rememberDialogButtonOk
import com.surzhykov.navsymphony.window.overlay.AnchorViewInfo
import com.surzhykov.navsymphony.window.overlay.HorizontalReferencePoint
import com.surzhykov.navsymphony.window.overlay.VerticalReferencePoint
import com.surzhykov.navsymphony.window.overlay.to
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

private val PaddingDefault = 16.dp
private val DialogIconSize = 48.dp

@Immutable
abstract class AbstractDialogWindow(
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

    abstract val severity: DrawableResolver
    abstract val titleResolver: StringResolver
    abstract val messageResolver: StringResolver?

    @Composable
    open fun rememberDialogButtons(): ImmutableList<DialogButton> {
        val overlayManager = LocalOverlayManager.current
        val okButton = rememberDialogButtonOk {
            dismissDialog(overlayManager)
        }
        return remember {
            persistentListOf(okButton)
        }
    }

    @Composable
    override fun BoxScope.RenderContent(modifier: Modifier) {
        val dialogScope = remember { DialogScope.Base() }
        dialogScope.CenteredDialogContent(modifier = Modifier) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = severity.resolve(),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.Top)
                        .padding(PaddingDefault)
                        .size(DialogIconSize),
                    tint = Color.Unspecified
                )
                Column(
                    modifier = Modifier
                        .padding(end = PaddingDefault),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    DialogTextBody(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(PaddingDefault)
                    )
                    DialogButtonsContent(
                        buttons = rememberDialogButtons(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                bottom = (PaddingDefault / 2),
                                top = (PaddingDefault + (PaddingDefault / 2))
                            )
                    )
                }
            }
        }
    }

    @Composable
    protected open fun DialogTextBody(modifier: Modifier) {
        DialogTextContent(
            titleResolver = titleResolver,
            messageResolver = messageResolver,
            modifier = modifier,
        )
    }

    protected fun dismissDialog(manager: OverlayManager) {
        if (onDismiss !== EmptyOnDismiss) onDismiss.invoke()
        manager.dismissOverlay(id)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AbstractDialogWindow) return false
        if (!super.equals(other)) return false

        if (onDismiss != other.onDismiss) return false
        if (severity != other.severity) return false
        if (titleResolver != other.titleResolver) return false
        if (messageResolver != other.messageResolver) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + onDismiss.hashCode()
        result = 31 * result + severity.hashCode()
        result = 31 * result + titleResolver.hashCode()
        result = 31 * result + (messageResolver?.hashCode() ?: 0)
        return result
    }

    companion object {
        @JvmStatic
        protected val EmptyOnDismiss = {}
    }
}