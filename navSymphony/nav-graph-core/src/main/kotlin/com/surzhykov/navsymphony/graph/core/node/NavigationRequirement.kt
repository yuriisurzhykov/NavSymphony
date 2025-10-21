package com.surzhykov.navsymphony.graph.core.node

/**
 * TO BE DEVELOPED!
 *
 * Represents a requirement that must be met before a navigation action can be performed.
 * This interface is a contract for defining conditions or prerequisites that need to be satisfied
 * before allowing a user to navigate to a specific destination or perform a navigation-related action.
 *
 * Examples of navigation requirements could include:
 * - User have an access to protected area.
 * - User must have completed a specific step in a process.
 * - User must have sufficient permissions.
 * - User must have accepted terms and conditions.
 *
 * Implementations of this interface will typically encapsulate the logic for checking
 * if the requirement is met and may include details on how to resolve the requirement if it is not.
 */
interface NavigationRequirement