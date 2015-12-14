package com.gmail.kei.aiwolf.viewer;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

import org.aiwolf.client.lib.TemplateTalkFactory.TalkType;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.client.lib.Utterance;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import jp.halfmoon.inaba.aiwolf.log.DayLog;
import jp.halfmoon.inaba.aiwolf.log.GameLog;
import jp.halfmoon.inaba.aiwolf.log.LogReader;
import jp.halfmoon.inaba.aiwolf.log.StatusLog;
import jp.halfmoon.inaba.aiwolf.log.TalkLog;

/**
 * コントローラークラス
 * @author keisuke
 *
 */
public class AiwolfController implements Initializable {

	//=============ログファイルのパスをここに設定してください===============
	public static final String filePath = null; // 指定なし -> ドラッグ&ドロップで読み込み
	// public static final String filePath = "log/Sample01.log"; // 指定あり -> 指定したログファイルを読み込み
	//=======================================================

	private GameLog log; // ゲームのログ情報

	@FXML
	private AnchorPane basePane; // 基本となるPane
	@FXML
	private StackPane graphPane; // グラフをプロットするPane
	@FXML
	private StackPane statusPane; // 各エージェントの状態を表示するPane
	@FXML
	private StackPane legendPane; // 凡例を表示するPane

	private Canvas graphCanvas; // グラフ描画用キャンバス
	private Canvas statusCanvas; // 状態描画用キャンバス
	private Canvas legendCanvas; // 凡例表示用キャンバス

	public AiwolfController() {
		// ログファイルの読み込み
		if (filePath != null) {
			log = LogReader.getLogData(filePath);
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// ログファイルが設定されている場合はその情報を描画
		if (log != null) {
			drawStatusPane(statusPane);
			drawGraphPane(graphPane);
		}
		// ログファイルが設定されていない場合はD&Dできることを表示
		else {
			drawMessage(graphPane);
		}
		drawLegendPane(legendPane); // 凡例を描画
	}

	// 凡例を描く処理
	private void drawLegendPane(StackPane parent) {
		final double WIDTH = 800.0; // キャンバスの幅
		final double HEIGHT = 20.0; // キャンバスの高さ
		Map<Topic, String> topicNameMap = new TreeMap<>(); // Topicとその名称を関連付けるMap
		Map<Topic, Color> topicColorMap = new TreeMap<>(); // Topicとその色を関連付けるMap

		mapTopicName(topicNameMap); // Topicとその名称を関連付け
		mapTopicColor(topicColorMap); // Topicとその色を関連付け

		// キャンバスを生成
		legendCanvas = new Canvas(WIDTH, HEIGHT);
		parent.getChildren().add(legendCanvas);

		// グラフィックスコンテキストを取得
		// 描画はグラフィックスコンテキストに対して行う
		GraphicsContext gc = legendCanvas.getGraphicsContext2D();

		// 背景を白で塗りつぶす
		gc.setFill(Color.WHITE);
		gc.fillRect(0, 0, WIDTH, HEIGHT);

		double diameter = 10.0; // 描く円の直径
		double drowPosX = 12.0; // 描画するX座標
		double drowPosY = 4.0; // 描画するY座標

		// 凡例を描画
		for (Topic topic : topicNameMap.keySet()) {
			gc.setFill(topicColorMap.get(topic));
			gc.fillOval(drowPosX, drowPosY, diameter, diameter);
			drowPosX += 15.0;
			gc.setFill(Color.BLACK);
			gc.fillText(topicNameMap.get(topic), drowPosX, drowPosY + 10.0);
			drowPosX += 55.0;
		}
	}

	// MapにTopicを鍵として、その名称を関連付ける
	private void mapTopicName(Map<Topic, String> map) {
		map.put(Topic.ESTIMATE, "予想");
		map.put(Topic.COMINGOUT, "告白");
		map.put(Topic.DIVINED, "占い");
		map.put(Topic.INQUESTED, "霊能");
		map.put(Topic.GUARDED, "護衛");
		map.put(Topic.VOTE, "投票");
		map.put(Topic.AGREE, "同意");
		map.put(Topic.DISAGREE, "反対");
		map.put(Topic.ATTACK, "攻撃");
		map.put(Topic.SKIP, "傍観");
		map.put(Topic.OVER, "終了");
	}

	// MapにTopicを鍵として、その色を関連付ける
	private void mapTopicColor(Map<Topic, Color> map) {
		map.put(Topic.ESTIMATE, Color.DARKVIOLET);
		map.put(Topic.COMINGOUT, Color.RED);
		map.put(Topic.DIVINED, Color.GREEN);
		map.put(Topic.INQUESTED, Color.BLUE);
		map.put(Topic.GUARDED, Color.BROWN);
		map.put(Topic.VOTE, Color.ORANGE);
		map.put(Topic.AGREE, Color.MEDIUMSEAGREEN);
		map.put(Topic.DISAGREE, Color.MEDIUMSLATEBLUE);
		map.put(Topic.ATTACK, Color.DARKGRAY);
		map.put(Topic.SKIP, Color.LIGHTBLUE);
		map.put(Topic.OVER, Color.STEELBLUE);
	}

	// エージェント名およびその役職を描く処理
	private void drawStatusPane(StackPane parent) {
		final double WIDTH = 200.0;
		final double HEIGHT = 580.0;
		int agentNum; // エージェントの人数
		double rowHeight; // 各行の高さ
		TreeMap<Integer, StatusLog> status; // 各エージェントの状態
		GraphicsContext graphicsContext;

		status = log.getDayLog(0).getStatus(); // 各エージェントの状態を取得
		agentNum = status.size(); // エージェントの数を取得
		rowHeight = HEIGHT / agentNum; // 各行の高さを計算

		if (!parent.getChildren().contains(statusCanvas)) {
			statusCanvas = new Canvas(WIDTH, HEIGHT);
			parent.getChildren().add(statusCanvas);
		}

		graphicsContext = statusCanvas.getGraphicsContext2D();
		graphicsContext.clearRect(0, 0, WIDTH, HEIGHT);

		double drawPosX = 0.0;
		double drawPosY = 0.0;

		// 白とグレーを交互に塗って縞を描く
		for (int i = 0; i < agentNum; i++) {
			graphicsContext.setFill(i % 2 != 0 ? Color.LIGHTGRAY : Color.WHITE);
			graphicsContext.fillRect(drawPosX, drawPosY, WIDTH, rowHeight);
			drawPosY += rowHeight;
		}

		// 各行にエージェント名を描く
		drawPosX = 5.0;
		drawPosY = rowHeight / 3.0;
		graphicsContext.setFill(Color.BLACK);
		for (StatusLog statusLog : status.values()) {
			String name = statusLog.getName();
			graphicsContext.fillText(name, drawPosX, drawPosY);
			drawPosY += rowHeight;
		}

		// 各行に役職を描く
		drawPosX = 5.0;
		drawPosY = rowHeight * 2.0 / 3.0 + 5.0;
		graphicsContext.setFill(Color.BLACK);
		for (StatusLog statusLog : status.values()) {
			String role = statusLog.getRole().toString();
			graphicsContext.fillText(role, drawPosX, drawPosY);
			drawPosY += rowHeight;
		}
	}

	// グラフを描く処理
	private void drawGraphPane(Pane parent) {
		final double WIDTH = 6000.0;
		final double HEIGHT = 580.0;

		if (!parent.getChildren().contains(graphCanvas)) {
			graphCanvas = new Canvas(WIDTH, HEIGHT);
			parent.getChildren().add(graphCanvas);
		}

		GraphicsContext gc = graphCanvas.getGraphicsContext2D();
		gc.clearRect(0, 0, WIDTH, HEIGHT);

		double drawPosX = 0.0;
		double drawPosY = 0.0;
		TreeMap<Integer, StatusLog> status = log.getDayLog(0).getStatus();
		int agentNum = status.size();
		double rowHeight = HEIGHT / agentNum;
		Map<Topic, Color> topicColorMap = new HashMap<>();
		mapTopicColor(topicColorMap);

		for (int i = 0; i < agentNum; i++) {
			gc.setFill(i % 2 != 0 ? Color.LIGHTGRAY : Color.WHITE);
			gc.fillRect(drawPosX, drawPosY, WIDTH, rowHeight);
			drawPosY += rowHeight;
		}

		double diameter = 10.0;

		drawPosX = 5.0;

		for (int day = 0; day <= log.getResult().getDay(); day++) {
			DayLog dayLog = log.getDayLog(day);
			ArrayList<TalkLog> talkLogList = dayLog.getTalk();

			List<Integer> overAgentNo = new ArrayList<>();
			gc.setStroke(Color.GRAY);
			gc.strokeLine(drawPosX, 0, drawPosX, HEIGHT); // 日付変更線
			for (TalkLog talkLog : talkLogList) {
				int num = talkLog.getAgentNo();
				Utterance utterance = new Utterance(talkLog.getContent());
				Topic topic = utterance.getTopic();

				// OVERの重複を除く
				if (overAgentNo.contains(num)) {
					continue;
				}
				if (topic == Topic.OVER) {
					overAgentNo.add(num);
				}
				drawPosY = rowHeight / 2.0 - diameter / 2.0 + (num - 1) * rowHeight;
				gc.setFill(topicColorMap.get(topic));
				if (talkLog.getTalkType() == TalkType.TALK) {
					gc.fillOval(drawPosX, drawPosY, diameter, diameter); // 会話プロット
					drawPosX += diameter * 3.0 / 2.0;
				} else {
					gc.fillRect(drawPosX, drawPosY, diameter, diameter); // ささやきプロット
					drawPosX += diameter * 3.0 / 2.0;
				}
			}
		}

		graphCanvas.setWidth(drawPosX + 10.0);
	}

	private void drawMessage(StackPane parent) {
		final double WIDTH = 6000.0;
		final double HEIGHT = 580.0;
		GraphicsContext graphicsContext;

		graphCanvas = new Canvas(WIDTH, HEIGHT);
		parent.getChildren().add(graphCanvas);
		graphicsContext = graphCanvas.getGraphicsContext2D();
		graphicsContext.setFont(new Font(30));
		graphicsContext.fillText("ログファイルをドラッグ&ドロップしてください", 10, 100);
	}

	// ファイルのドロップを受け付ける処理
	public void handleDragOver(DragEvent event) {
		// ファイルの場合だけ受け付ける
		Dragboard dragboard = event.getDragboard();
		if (dragboard.hasFiles()) {
			event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
		}
		event.consume();
	}

	public void handDragDropped(DragEvent event) {
		boolean success = false;

		// ファイルの場合だけ受け付ける
		Dragboard dragboard = event.getDragboard();
		if (dragboard.hasFiles()) {
			List<File> list = dragboard.getFiles();

			log = LogReader.getLogData(list.get(0).getPath());
			drawStatusPane(statusPane);
			drawGraphPane(graphPane);

			success = true;
		}
		event.setDropCompleted(success);
		event.consume();
	}

}
