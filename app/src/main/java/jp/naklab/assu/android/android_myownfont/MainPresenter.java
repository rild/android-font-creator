package jp.naklab.assu.android.android_myownfont;

import android.content.Context;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import jp.naklab.assu.android.android_myownfont.entity.Player;

public class MainPresenter {
    Context context;
    ArrayList<Player> players = new ArrayList<>();

    public MainPresenter(Context context) {
        this.context = context;
    }

    // xml ファイルを読み取って、 textView に表示させる
    public String parseXML2String() {
        XmlPullParserFactory parserFactory;
        try {
            parserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserFactory.newPullParser();
            InputStream is = context.getAssets().open("sample_player.xml");
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(is, null);

            return processParsing(parser);

        } catch (XmlPullParserException e) {

        } catch (IOException e) {
        }
        return  "";
    }

    // xml ファイルのタグ情報を元に Parse する
    private String processParsing(XmlPullParser parser) throws IOException, XmlPullParserException{
        int eventType = parser.getEventType();
        Player currentPlayer = null;

        while (eventType != XmlPullParser.END_DOCUMENT) {
            String eltName = null;

            switch (eventType) {
                case XmlPullParser.START_TAG:
                    eltName = parser.getName();

                    if ("player".equals(eltName)) {
                        currentPlayer = new Player();
                        players.add(currentPlayer);
                    } else if (currentPlayer != null) {
                        if ("name".equals(eltName)) {
                            currentPlayer.name = parser.nextText();
                        } else if ("age".equals(eltName)) {
                            currentPlayer.age = parser.nextText();
                        } else if ("position".equals(eltName)) {
                            currentPlayer.position = parser.nextText();
                        }
                    }
                    break;
            }

            eventType = parser.next();
        }

        return loadPlayersString(players);
    }

    private String loadPlayersString(ArrayList<Player> players) {
        StringBuilder builder = new StringBuilder();

        for (Player player : players) {
            builder.append(player.name).append("\n").
                    append(player.age).append("\n").
                    append(player.position).append("\n\n");
        }

        return builder.toString();
    }
}
