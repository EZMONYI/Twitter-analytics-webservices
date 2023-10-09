package com.clouddeadline.BlockChain;
import io.jooby.Jooby;
import org.json.simple.JSONObject;

public class App extends Jooby {

  {
    get("/blockchain", ctx -> {
      BlockChain blockChainHelper = new BlockChain();
      if(ctx.query("cc").isMissing()) {
        return "wrong key";
      }
      String data = ctx.query("cc").value();
      String encoded = blockChainHelper.validateAndAddBlockChain(data);
      return "CloudDeadline,576651666672\n"+encoded;

    });
  }

  public static void main(final String[] args) {
    runApp(args, App::new);
  }

}
