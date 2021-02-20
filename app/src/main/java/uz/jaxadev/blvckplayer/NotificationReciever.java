package uz.jaxadev.blvckplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReciever extends BroadcastReceiver {
    public static final String CLICK_PLAY = "actionplay";
    public static final String CLICK_NEXT = "actionnext";
    public static final String CLICK_PREVIOUS = "actionprevious";
    public static final String CLICK_DELETE = "actiondelete";
    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case CLICK_PLAY:
                intent = new Intent(context, MusicService.class);
                intent.putExtra("actionname", CLICK_PLAY);
                context.startService(intent);
                //Toast.makeText(context, "NOTIFY_PLAY", Toast.LENGTH_LONG).show();
                break;
            case CLICK_NEXT:
                intent = new Intent(context, MusicService.class);
                intent.putExtra("actionname", CLICK_NEXT);
                context.startService(intent);
                //Toast.makeText(context, "NOTIFY_NEXT", Toast.LENGTH_LONG).show();
                break;
            case CLICK_PREVIOUS:
                intent = new Intent(context, MusicService.class);
                intent.putExtra("actionname", CLICK_PREVIOUS);
                context.startService(intent);
                //Toast.makeText(context, "NOTIFY_PREVIOUS", Toast.LENGTH_LONG).show();
                break;
            case CLICK_DELETE:
                intent = new Intent(context, MusicService.class);
                intent.putExtra("actionname", CLICK_DELETE);
                context.startService(intent);
                //Toast.makeText(context, "NOTIFY_PREVIOUS", Toast.LENGTH_LONG).show();
                break;
        }
    }
}
