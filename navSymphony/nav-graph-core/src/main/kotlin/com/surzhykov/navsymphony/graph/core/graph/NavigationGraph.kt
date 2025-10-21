package com.surzhykov.navsymphony.graph.core.graph

import androidx.navigation.NavHostController
import com.surzhykov.navsymphony.graph.core.node.NavigationNode
import com.surzhykov.navsymphony.graph.core.node.NodeId
import com.surzhykov.navsymphony.screen.core.ScreenRoute
import java.util.LinkedList
import kotlin.reflect.KClass

/**
 * [NavigationGraph] interface defines the structure and behavior of a navigation graph.
 *
 * A navigation graph is a hierarchical structure representing the navigation flow within an
 * application. It consists of [NavigationNode]s, each representing a screen or a part of the
 * navigation hierarchy.
 *
 * This interface provides methods to:
 *  - Configure the navigation with a [NavHostController].
 *  - Traverse the graph to perform operations on each [NavigationNode].
 *  - Retrieve a specific [NavigationNode] based on its [ScreenRoute] class.
 */
@NavigationGraphDsl
interface NavigationGraph {

    val rootClass: KClass<out ScreenRoute>

    val rootNode: NavigationNode

    /**
     * Traverses the navigation tree rooted at this node and performs the specified [action] on each node.
     *
     * This function performs a depth-first traversal of the navigation tree.
     * The provided [action] lambda is invoked for each node encountered during the traversal.
     *
     * @param action The lambda function to be executed for each node in the navigation tree.
     *  This lambda takes a [NavigationNode] as input and has no return value. It allows you to
     *  perform custom operations on each node during the traversal,
     *  such as logging, validation, or modification.
     *
     * Example:
     * ```kotlin
     * val root = NavigationNode("Root")
     * val child1 = NavigationNode("Child1")
     * val child2 = NavigationNode("Child2")
     * root.children.addAll(listOf(child1, child2))
     *
     * root.traverse { node ->
     *     println("Visited node: ${node.name}")
     * }
     * // Expected Output (order may vary as it depends on children order):
     * // Visited node: Root
     * // Visited node: Child1
     * // Visited node: Child2
     * ```
     */
    fun traverse(action: (NavigationNode) -> Unit)

    /**
     * Retrieves a [NavigationNode] associated with a given [ScreenRoute] class.
     *
     * This function searches for a previously registered [NavigationNode] that is
     * associated with the provided [ScreenRoute] class [klass]. If a matching node
     * is found, it is returned. Otherwise, `null` is returned, indicating that no
     * node has been registered for that route.
     *
     * @param klass The [KClass] representing the [ScreenRoute] for which to find the associated
     * [NavigationNode].
     * @param R The specific type of [ScreenRoute] represented by the [KClass].
     *
     * @return The [NavigationNode] associated with the provided [ScreenRoute] class, or `null`
     * if no such node is found.
     *
     * @see NavigationNode
     * @see ScreenRoute
     */
    operator fun <R : ScreenRoute> get(klass: KClass<R>): NavigationNode?

    /**
     * Retrieves a [NavigationNode.MenuNode] associated with a given [ScreenRoute.Menu] class.
     *
     * This function searches for a menu node within the navigation structure that corresponds to
     * the provided [klass] (which represents a specific [ScreenRoute.Menu]). If a matching menu
     * node is found, it's returned; otherwise, `null` is returned.
     *
     * This is typically used to access and manipulate menu-specific navigation information based on
     * the route type.
     *
     * @param klass The [KClass] representing the [ScreenRoute.Menu] for which to find the menu node.
     * @param R A type parameter constrained to [ScreenRoute.Menu], indicating the specific type of
     *          menu route being searched for.
     * @return A [NavigationNode.MenuNode] corresponding to the given [klass] if found, `null`
     * otherwise.
     * */
    fun <R : ScreenRoute.Menu> getMenu(klass: KClass<R>): Result<NavigationNode.MenuNode>

    /**
     * Flattens the navigation tree rooted at this node into a list of all its descendants and
     * itself.
     *
     * This function performs a depth-first traversal of the navigation tree, starting from the
     * current node. It visits each node and adds it to the resulting list, ensuring that all nodes
     * in the tree are included.
     *
     * The order of nodes in the returned list corresponds to the depth-first traversal order.
     *
     * @return A list containing all nodes in the navigation tree, including the root node.
     *
     * Example:
     * ```
     * // Assuming a navigation tree structure like:
     * //     A
     * //    / \
     * //   B   C
     * //  / \   \
     * // D   E   F
     *
     * val nodeA = NavigationNode("A", listOf(nodeB, nodeC))
     * val nodeB = NavigationNode("B", listOf(nodeD, nodeE))
     * val nodeC = NavigationNode("C", listOf(nodeF))
     * val nodeD = NavigationNode("D")
     * val nodeE = NavigationNode("E")
     * val nodeF = NavigationNode("F")
     *
     * val flattened = nodeA.flattenNodes()
     * // flattened will be [nodeA, nodeB, nodeD, nodeE, nodeC, nodeF]
     * ```
     */
    fun flattenNodes(): List<NavigationNode>

    /**
     * Represents the base navigation graph structure.
     *
     * @see NavigationGraph
     * @see NavigationNode
     * @see NavigationGraphDsl
     * @see ScreenRoute
     * @see NodeId
     */
    @NavigationGraphDsl
    class Base(
        private val nodes: LinkedList<NavigationNode>,
        override val rootClass: KClass<out ScreenRoute>,
    ) : NavigationGraph {

        private val nodeMap: Map<NodeId, NavigationNode> = flattenNodes().associateBy {
            it.nodeMetaData.nodeId
        }

        init {
            check(nodes.isNotEmpty()) {
                "Navigation graph must have at least one node"
            }
            check(nodeMap.containsKey(NodeId(rootClass))) {
                "Root node not found in navigation graph!"
            }
        }

        override val rootNode: NavigationNode by lazy {
            getNode(NodeId(rootClass))!!
        }

        override operator fun <R : ScreenRoute> get(klass: KClass<R>): NavigationNode? =
            getNode(NodeId(klass))

        override fun <R : ScreenRoute.Menu> getMenu(klass: KClass<R>): Result<NavigationNode.MenuNode> {
            val relatedNode = get(klass)
            return if (relatedNode is NavigationNode.MenuNode) {
                Result.success(relatedNode)
            } else {
                Result.failure(IllegalStateException("Node is not a menu node"))
            }
        }

        override fun traverse(action: (NavigationNode) -> Unit) = flattenNodes().forEach(action)

        override fun flattenNodes(): List<NavigationNode> {
            val result = mutableListOf<NavigationNode>()
            fun travers(node: NavigationNode) {
                result.add(node)
                if (node is NavigationNode.MenuNode) {
                    node.children.forEach { travers(it) }
                }
            }
            nodes.forEach { travers(it) }
            return result
        }

        private fun getNode(nodeId: NodeId): NavigationNode? = nodeMap[nodeId]
    }
}