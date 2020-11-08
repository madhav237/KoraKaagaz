package processing.server.board;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import infrastructure.validation.logger.LogLevel;
import infrastructure.validation.logger.ModuleID;
import processing.utility.*;
import networking.INotificationHandler;
import processing.*;

/**
 * This class implements INotificationHandlerr and 
 * handles the stop request from the client when they
 * exit their application.
 * 
 * @author Himanshu Jain
 * @reviewer Ahmed Zaheer Dadarkar
 *
 */

public class StopRequestHandler implements INotificationHandler {
	
	/**
	 * onMessageRecived function of INotificationHandler which will be called
	 * by the networking module whenever any message they have to send here with
	 * this particular identifier to stop the connection from a particular client. 
	 */
	public void onMessageReceived(String message) {
		
		ClientBoardState.logger.log(
				ModuleID.PROCESSING, 
				LogLevel.INFO, 
				"Received stop connection request from a client on the server"
		);
		
		/**
		 * As the client is closing it's application we need to remove it from 
		 * the list of all the clients connected to this board.
		 */
		ClientBoardState.users.remove(new Username(message));
		
		/**
		 * If after leaving this client user list becomes empty i.e no client is connected
		 * to this board server, we need to close this server and inform the main server as
		 * well.
		 */
		if(ClientBoardState.users.isEmpty()) {
			
			String persistence = null;
			
			/**
			 * As this Board Server is going to shut, we need to save the data of this server
			 * for persistence, thus serializing the maps in ClientBoardState
			 */
			try {
				persistence = Serialize.serialize(ClientBoardState.maps);
			} catch (IOException e) {
				
				ClientBoardState.logger.log(
						ModuleID.PROCESSING, 
						LogLevel.ERROR, 
						"IO Exception occured during serializing BoardState"
				);
				
			}
			
			/**
			 * Saving the serialized the data in the file with name same as the BoardID.
			 */
			try {
				PersistanceSupport.storeStateString(persistence, ClientBoardState.boardId);
			} catch (UnsupportedEncodingException e) {
				
				ClientBoardState.logger.log(
						ModuleID.PROCESSING, 
						LogLevel.ERROR, 
						"UnsupportedEncodingException occured while saving the persistence state"
				);
				
			} catch (IOException e) {
				
				ClientBoardState.logger.log(
						ModuleID.PROCESSING, 
						LogLevel.ERROR, 
						"IO Exception occured during saving the persistence state"
				);
				
			}
			
			ClientBoardState.logger.log(
					ModuleID.PROCESSING, 
					LogLevel.SUCCESS, 
					"Successfully saved the persistence file in the local machine"
			);
			
			/**
			 * Notifying the networking module to stop listening for this server
			 * messages by calling their stop function.
			 */
			ClientBoardState.communicator.stop();
			
			/**
			 * Notifying the main server to shut this board's server by passing board ID as
			 * the argument.
			 */
			String mainServerAddress = ClientBoardState.userIP.toString()
									 + ":"
									 + Integer.toString(8467);
			
			ClientBoardState.send(
					mainServerAddress, 
					ClientBoardState.boardId.toString(), 
					"RemoveBoard"
			);
			
			System.exit(0);
		}
	}
	
}
