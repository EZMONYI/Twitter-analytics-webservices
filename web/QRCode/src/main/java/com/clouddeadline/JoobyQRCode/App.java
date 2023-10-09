package com.clouddeadline.JoobyQRCode;

import io.jooby.Jooby;

public class App extends Jooby {

  {
    get("/qrcode", ctx -> {
      QrCodeService qrCodeService = new QrCodeService();
      if(ctx.query("type").isMissing()) {
        return "action type is missing";
      }
      if(ctx.query("data").isMissing()){
        return "data is missing";
      }
      if(ctx.query("type").value().equals("encode")){
        return qrCodeService.encode(ctx.query("data").value());
      }else if(ctx.query("type").value().equals("decode")){
        return qrCodeService.decode(ctx.query("data").value());
      }else{
        return "action type has to be either encode or decode";
      }

    });
  }

  public static void main(final String[] args) {
    runApp(args, App::new);
  }

}
