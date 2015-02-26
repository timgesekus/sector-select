package utils;
import play.mvc.WebSocket;


public class WebSocketUtils {
	public static WebSocket<String> notAuthorizedWebSocket() {
		return new WebSocket<String>(){
		    @Override
		    public void onReady(play.mvc.WebSocket.In<String> in,
		            final play.mvc.WebSocket.Out<String> out) {
		        out.write("Forbidden");
		        out.close();
		    }
		};
	}
}
