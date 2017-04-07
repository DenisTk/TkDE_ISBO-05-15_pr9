package p1;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

import static com.sun.org.apache.xalan.internal.xsltc.compiler.sym.error;

public class First_main {
    public static void main(String[] args) throws Exception{
        AsynchronousChannelGroup group= AsynchronousChannelGroup.withThreadPool(Executors.newFixedThreadPool(10));
        AsynchronousServerSocketChannel ass= AsynchronousServerSocketChannel.open(group);
        ass.bind(new InetSocketAddress(80));
        ass.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {

            @Override
            public void completed(AsynchronousSocketChannel result, Void attachment) {
                ass.accept(null, this);
                process(result);

            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                exc.printStackTrace();

            }
        });
        while(true){
            Thread.sleep(1000);
        }
        
}
  public static void process(AsynchronousSocketChannel s){
      ByteBuffer buf=ByteBuffer.allocate(10240);
      StringBuilder request=new StringBuilder();
      s.read(buf, null, new CompletionHandler<Integer, Void>() {

          @Override
          public void completed(Integer result, Void attachment) {
              buf.flip();
              byte [] data=new byte[buf.remaining()];
              buf.get(data);
              request.append(new String(data, StandardCharsets.US_ASCII));
              int len=request.length();
              if (len>=4 && request.substring(len-4).equals("\r\n\r\n")){
                  System.out.println(request);
                  SendResponse(s);
              } else{
                  buf.clear();
                  s.read(buf,null,this);

              }
          }

          @Override
          public void failed(Throwable exc, Void attachment) {
              exc.printStackTrace();

          }
      });
  }

    private static void SendResponse(AsynchronousSocketChannel s) {
      ByteBuffer bf=ByteBuffer.wrap("Hello Word".getBytes());
      s.write(bf, null, new CompletionHandler<Integer, Void>() {
          @Override
          public void completed(Integer result, Void attachment) {
              try{
                  s.close();
              } catch (IOException e){
                  e.printStackTrace();
              }

          }

          @Override
          public void failed(Throwable exc, Void attachment) {

          }
      });
    }
}
