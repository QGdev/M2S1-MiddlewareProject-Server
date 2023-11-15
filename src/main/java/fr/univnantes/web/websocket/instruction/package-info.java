/**
 * Package containing all instructions that are communicated between the server and clients.
 *
 * <p>
 * This package includes the following classes:
 * <ul>
 * <li>{@link fr.univnantes.web.websocket.instruction.ConnectInstruction} - Instruction to connect.
 * <li>{@link fr.univnantes.web.websocket.instruction.DeleteCharInstruction} - Instruction to delete a character in a document.
 * <li>{@link fr.univnantes.web.websocket.instruction.DeleteLineBrkInstruction} - Instruction to delete a line in a document.
 * <li>{@link fr.univnantes.web.websocket.instruction.InsertCharInstruction} - Instruction allowing you to insert a character into a document.
 * <li>{@link fr.univnantes.web.websocket.instruction.InsertLineBrkInstruction} - Instruction allowing you to insert a line into a document.
 * <li>{@link fr.univnantes.web.websocket.instruction.InstructionType} - Enum of different types of instructions.
 * <li>{@link fr.univnantes.web.websocket.instruction.Utils} - Class implementing a method for generating an error message.
 * <li>{@link fr.univnantes.web.websocket.instruction.WebSocketInstruction} - Interface allowing encapsulation of common methods between different instructions.
 * </ul>
 */
package fr.univnantes.web.websocket.instruction;