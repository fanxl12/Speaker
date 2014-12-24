package com.fanxl.speaker;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

public class MainActivity extends Activity {
	
	// 语音合成对象
	private SpeechSynthesizer mTts;
	// 默认发音人
	private String voicer="vixq";
	private EditText speak_text;
//	//缓冲进度
//	private int mPercentForBuffering = 0;	
//	//播放进度
//	private int mPercentForPlaying = 0;
	
	//语音听写对象
	private SpeechRecognizer mIat;
	// 语音听写UI
	private RecognizerDialog iatDialog;
	private boolean isShowDialog = true;
	
	

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initVariable();
    }


	private void initView() {
		speak_text = (EditText) findViewById(R.id.speak_text);
	}


	private void initVariable() {
		// 将“12345678”替换成您申请的 APPID，申请地址：http://open.voicecloud.cn 
		SpeechUtility.createUtility(getApplicationContext(), SpeechConstant.APPID +"=5498cc8e"); 
		// 初始化合成对象
		mTts = SpeechSynthesizer.createSynthesizer(this, null);
		
		mIat = SpeechRecognizer.createRecognizer(this, null); 
		
		// 初始化听写Dialog,如果只使用有UI听写功能,无需创建SpeechRecognizer
		iatDialog = new RecognizerDialog(this, null);
	}
	
	public void speak(View view){
		String text = speak_text.getText().toString().trim();
		if(TextUtils.isEmpty(text)){
			showTip("合成内容不能为空");
			return;
		}
		setParam();
		int code = mTts.startSpeaking(text, mTtsListener);
		if (code != ErrorCode.SUCCESS) {
			if(code == ErrorCode.ERROR_COMPONENT_NOT_INSTALLED){
				//未安装则跳转到提示安装页面
				showTip("需要安装助手");
			}else {
				showTip("语音合成失败,错误码: " + code);	
			}
		}
	}
	
	public void listen(View view){
		speak_text.setText(null);
		setListenParam();
		int ret = 0;// 函数调用返回值
		if (isShowDialog) {
			// 显示听写对话框
			iatDialog.setListener(recognizerDialogListener);
			iatDialog.show();
			showTip("请开始说话");
		} else {
			// 不显示听写对话框
			ret = mIat.startListening(mRecoListener);
			if(ret != ErrorCode.SUCCESS){
				showTip("听写失败,错误码：" + ret);
			}else {
				showTip("请开始说话");
			}
		}
	}
	
	private void showTip(String string) {
		Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
	}


	
	/**
	 * 参数设置
	 * @param param
	 * @return 
	 */
	private void setParam(){
		
		//设置合成
		mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
		//设置发音人
		mTts.setParameter(SpeechConstant.VOICE_NAME, voicer);
		
		//设置语速
		mTts.setParameter(SpeechConstant.SPEED, "50");

		//设置音调
		mTts.setParameter(SpeechConstant.PITCH, "50");

		//设置音量
		mTts.setParameter(SpeechConstant.VOLUME, "50");
		
		//设置播放器音频流类型
		mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");
	}
	
	private void setListenParam(){
		//2.设置听写参数，详见《科大讯飞MSC API手册(Android)》SpeechConstant类 
		mIat.setParameter(SpeechConstant.DOMAIN, "iat"); 
		mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn"); 
		mIat.setParameter(SpeechConstant.ACCENT, "mandarin "); 
	}
	
	
	/**
	 * 合成回调监听。
	 */
	private SynthesizerListener mTtsListener = new SynthesizerListener() {
		@Override
		public void onSpeakBegin() {
			showTip("开始播放");
		}

		@Override
		public void onSpeakPaused() {
			showTip("暂停播放");
		}

		@Override
		public void onSpeakResumed() {
			showTip("继续播放");
		}

		@Override
		public void onBufferProgress(int percent, int beginPos, int endPos,
				String info) {
//			mPercentForBuffering = percent;
//			showTip(String.format("缓冲进度为%d%%，播放进度为%d%%", mPercentForBuffering, mPercentForPlaying));
		}

		@Override
		public void onSpeakProgress(int percent, int beginPos, int endPos) {
//			mPercentForPlaying = percent;
//			showTip(String.format("缓冲进度为%d%%，播放进度为%d%%", mPercentForBuffering, mPercentForPlaying));
		}

		@Override
		public void onCompleted(SpeechError error) {
			if(error == null)
			{
				showTip("播放完成");
			}
			else if(error != null)
			{
				showTip(error.getPlainDescription(true));
			}
		}
	};
	
	/**
	 * 听写UI监听器
	 */
	private RecognizerDialogListener recognizerDialogListener=new RecognizerDialogListener(){
		public void onResult(RecognizerResult results, boolean isLast) {
			String text = JsonParser.parseIatResult(results.getResultString());
			speak_text.append(text);
			speak_text.setSelection(speak_text.length());
		}

		/**
		 * 识别回调错误.
		 */
		public void onError(SpeechError error) {
			showTip(error.getPlainDescription(true));
		}

	};
	
	
	//听写监听器  不显示对话框
	private RecognizerListener mRecoListener = new RecognizerListener(){ 
		//听写结果回调接口(返回Json格式结果，用户可参见附录)； 
		//一般情况下会通过onResults接口多次返回结果，完整的识别内容是多次结果的累加； 
		//关于解析Json的代码可参见MscDemo中JsonParser类；
		//isLast等于true时会话结束。 
		public void onResult(RecognizerResult results, boolean isLast) {   
			
			String text = JsonParser.parseIatResult(results.getResultString());
			speak_text.append(text);
			speak_text.setSelection(speak_text.length());
		} 
		//会话发生错误回调接口  
		public void onError(SpeechError error) { 
			//获取错误码描述
			error.getPlainDescription(true); 
		}  
		//开始录音 
		public void onBeginOfSpeech() {
			showTip("听写开始");
		}  
		//音量值0~30  
		public void onVolumeChanged(int volume){
			
		}  
		//结束录音  
		public void onEndOfSpeech() {
			showTip("听写完毕");
		}  
		//扩展用接口  
		public void onEvent(int eventType,int arg1,int arg2,String msg) {
			
		} 
		
	}; 
		
		
		
		
	
	
	
	/**
	 * 初期化监听。
	 */
//	private InitListener mTtsInitListener = new InitListener() {
//		@Override
//		public void onInit(int code) {
//			if (code == ErrorCode.SUCCESS) {
//				showTip("初始化成功");
//			}			
//		}
//	};
}
