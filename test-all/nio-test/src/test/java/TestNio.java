import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class TestNio {
    @Test
    public void client() throws IOException {
        SocketChannel open = SocketChannel.open();
        open.connect(new InetSocketAddress("127.0.0.1",8081));

    }
}
