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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.surzhykov.navsymphony.core.presentation.DrawableResolver
import com.surzhykov.navsymphony.core.presentation.StringResolver
import com.surzhykov.navsymphony.window.core.AbstractOverlayWindow
import com.surzhykov.navsymphony.window.overlay.AnchorViewInfo
import com.surzhykov.navsymphony.window.overlay.HorizontalReferencePoint
import com.surzhykov.navsymphony.window.overlay.VerticalReferencePoint
import com.surzhykov.navsymphony.window.overlay.to
import kotlinx.collections.immutable.ImmutableList

/**
 * Internal implementation of the primary dialog overlay.
 *
 * [PrimaryDialogWindow] extends [AbstractOverlayWindow] and is responsible for rendering the full dialog.
 * It displays:
 * - An icon at the top.
 * - A text content area (with a title and an optional message) that is scrollable if needed.
 * - A row of dialog buttons.
 *
 * **Corner Cases:**
 * - The [buttonSpecs] list must not be empty; otherwise, an [IllegalArgumentException] is thrown.
 * - If [messageResolver] is null, only the title is displayed.
 *
 * **Usage Example:**
 * ```
 * val dialogOverlay = PrimaryDialogWindow(
 *     iconResolver = myIconResolver,
 *     titleResolver = stringResolver("Dialog Title"),
 *     messageResolver = stringResolver("Optional message"),
 *     buttonSpecs = myButtonList,
 *     anchorViewInfo = myAnchorInfo,
 *     onDismiss = { /* Dismiss dialog */ }
 * )
 * // Show overlay using your overlay manager:
 * overlayManager.showOverlay(dialogOverlay)
 * ```
 *
 * @property iconResolver Resolver for the dialog icon.
 * @property titleResolver Resolver for the dialog title.
 * @property messageResolver Optional resolver for the dialog message.
 * @property buttonSpecs A non-empty list of [DialogButton] instances.
 * @property anchorViewInfo Anchor information for positioning the dialog.
 * @property id Unique identifier for the dialog overlay.
 * @property onDismiss Callback invoked when the dialog is dismissed.
 */
@Immutable
data class PrimaryDialogWindow(
    private val iconResolver: DrawableResolver,
    private val titleResolver: StringResolver,
    private val messageResolver: StringResolver?,
    private val buttonSpecs: ImmutableList<DialogButton>,
    override val onDismiss: () -> Unit,
) : AbstractOverlayWindow(
    anchorViewInfo = AnchorViewInfo.Base(
        DpSize.Zero,
        DpOffset.Zero,
        HorizontalReferencePoint.Center to HorizontalReferencePoint.Center,
        VerticalReferencePoint.Center to VerticalReferencePoint.Center
    ),
    onDismiss = onDismiss
) {

    init {
        require(buttonSpecs.isNotEmpty()) {
            "Button specs list should not be empty, " +
                    "otherwise user will not be able to interact with dialog"
        }
    }

    /**
     * Renders the dialog overlay content.
     *
     * The content consists of:
     * - An icon (aligned at the top).
     * - A column that contains a scrollable text area and a row of buttons.
     *
     * **Usage Example:**
     * ```
     * // Called by the overlay manager when the dialog is shown.
     * dialogOverlay.RenderContent(modifier = Modifier)
     * ```
     *
     * @param modifier A [Modifier] for customizing the content container.
     */
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
                        buttons = buttonSpecs,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Renders the text content for the dialog, including the title and an optional message.
 * The title is limited to two lines and the message (if present) is rendered with ellipsis overflow.
 *
 * @param titleResolver Resolver for the title text.
 * @param messageResolver Optional resolver for the message text.
 * @param modifier A [Modifier] for container customization.
 */
@Composable
fun DialogTextContent(
    titleResolver: StringResolver,
    messageResolver: StringResolver?,
    modifier: Modifier = Modifier,
    titleStyle: TextStyle = MaterialTheme.typography.titleLarge,
    messageStyle: TextStyle = MaterialTheme.typography.bodyLarge,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = titleResolver.asString(),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = titleStyle,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        if (messageResolver != null) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                text = messageResolver.asString(),
                overflow = TextOverflow.Ellipsis,
                style = messageStyle,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Renders a row of dialog buttons.
 * The buttons are evenly distributed across the available width.
 *
 * @param buttons List of [DialogButton] specifications.
 * @param modifier A [Modifier] for customizing the button row.
 */
@Composable
fun DialogButtonsContent(
    buttons: ImmutableList<DialogButton>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (buttons.size == 1) Arrangement.Center else Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        buttons.forEach { button ->
            button.Render(Modifier)
        }
    }
}