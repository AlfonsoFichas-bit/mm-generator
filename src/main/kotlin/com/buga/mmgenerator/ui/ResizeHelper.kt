package com.buga.mmgenerator.ui

import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.event.EventType
import javafx.scene.Cursor
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.input.MouseEvent
import javafx.stage.Stage

object ResizeHelper {

    fun addResizeListener(stage: Stage) {
        val resizeListener = ResizeListener(stage)
        stage.scene.addEventHandler(MouseEvent.MOUSE_MOVED, resizeListener)
        stage.scene.addEventHandler(MouseEvent.MOUSE_PRESSED, resizeListener)
        stage.scene.addEventHandler(MouseEvent.MOUSE_DRAGGED, resizeListener)
        stage.scene.addEventHandler(MouseEvent.MOUSE_EXITED, resizeListener)
        stage.scene.addEventHandler(MouseEvent.MOUSE_EXITED_TARGET, resizeListener)

        val children = stage.scene.root.childrenUnmodifiable
        for (child in children) {
            addListenerDeeply(child, resizeListener)
        }
    }

    private fun addListenerDeeply(node: Node, listener: EventHandler<MouseEvent>) {
        node.addEventHandler(MouseEvent.MOUSE_MOVED, listener)
        node.addEventHandler(MouseEvent.MOUSE_PRESSED, listener)
        node.addEventHandler(MouseEvent.MOUSE_DRAGGED, listener)
        node.addEventHandler(MouseEvent.MOUSE_EXITED, listener)
        node.addEventHandler(MouseEvent.MOUSE_EXITED_TARGET, listener)
        if (node is Parent) {
            val children: ObservableList<Node> = node.childrenUnmodifiable
            for (child in children) {
                addListenerDeeply(child, listener)
            }
        }
    }

    private class ResizeListener(private val stage: Stage) : EventHandler<MouseEvent> {
        private var cursorEvent = Cursor.DEFAULT
        private val border = 6
        private var startX = 0.0
        private var startY = 0.0

        override fun handle(mouseEvent: MouseEvent) {
            val mouseEventType: EventType<out MouseEvent> = mouseEvent.eventType
            val scene: Scene = stage.scene

            val mouseEventX = mouseEvent.sceneX
            val mouseEventY = mouseEvent.sceneY
            val sceneWidth = scene.width
            val sceneHeight = scene.height

            if (MouseEvent.MOUSE_MOVED == mouseEventType) {
                if (stage.isMaximized) {
                    scene.cursor = Cursor.DEFAULT
                    return
                }

                cursorEvent = when {
                    mouseEventX < border && mouseEventY < border -> Cursor.NW_RESIZE
                    mouseEventX < border && mouseEventY > sceneHeight - border -> Cursor.SW_RESIZE
                    mouseEventX > sceneWidth - border && mouseEventY < border -> Cursor.NE_RESIZE
                    mouseEventX > sceneWidth - border && mouseEventY > sceneHeight - border -> Cursor.SE_RESIZE
                    mouseEventX < border -> Cursor.W_RESIZE
                    mouseEventX > sceneWidth - border -> Cursor.E_RESIZE
                    mouseEventY < border -> Cursor.N_RESIZE
                    mouseEventY > sceneHeight - border -> Cursor.S_RESIZE
                    else -> Cursor.DEFAULT
                }
                scene.cursor = cursorEvent
            } else if (MouseEvent.MOUSE_EXITED == mouseEventType || MouseEvent.MOUSE_EXITED_TARGET == mouseEventType) {
                scene.cursor = Cursor.DEFAULT
            } else if (MouseEvent.MOUSE_PRESSED == mouseEventType) {
                startX = stage.width - mouseEventX
                startY = stage.height - mouseEventY
            } else if (MouseEvent.MOUSE_DRAGGED == mouseEventType) {
                if (Cursor.DEFAULT != cursorEvent && !stage.isMaximized) {
                    if (Cursor.W_RESIZE == cursorEvent || Cursor.NW_RESIZE == cursorEvent || Cursor.SW_RESIZE == cursorEvent) {
                        // Resizing from left
                    } else if (Cursor.E_RESIZE == cursorEvent || Cursor.NE_RESIZE == cursorEvent || Cursor.SE_RESIZE == cursorEvent) {
                        val newWidth = mouseEventX + startX
                        if (newWidth > stage.minWidth) {
                            stage.width = newWidth
                        }
                    }

                    if (Cursor.N_RESIZE == cursorEvent || Cursor.NW_RESIZE == cursorEvent || Cursor.NE_RESIZE == cursorEvent) {
                        // Resizing from top
                    } else if (Cursor.S_RESIZE == cursorEvent || Cursor.SW_RESIZE == cursorEvent || Cursor.SE_RESIZE == cursorEvent) {
                        val newHeight = mouseEventY + startY
                        if (newHeight > stage.minHeight) {
                            stage.height = newHeight
                        }
                    }
                }
            }
        }
    }
}
