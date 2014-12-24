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
	
	// �����ϳɶ���
	private SpeechSynthesizer mTts;
	// Ĭ�Ϸ�����
	private String voicer="vixq";
	private EditText speak_text;
//	//�������
//	private int mPercentForBuffering = 0;	
//	//���Ž���
//	private int mPercentForPlaying = 0;
	
	//������д����
	private SpeechRecognizer mIat;
	// ������дUI
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
		// ����12345678���滻��������� APPID�������ַ��http://open.voicecloud.cn 
		SpeechUtility.createUtility(getApplicationContext(), SpeechConstant.APPID +"=5498cc8e"); 
		// ��ʼ���ϳɶ���
		mTts = SpeechSynthesizer.createSynthesizer(this, null);
		
		mIat = SpeechRecognizer.createRecognizer(this, null); 
		
		// ��ʼ����дDialog,���ֻʹ����UI��д����,���贴��SpeechRecognizer
		iatDialog = new RecognizerDialog(this, null);
	}
	
	public void speak(View view){
		String text = speak_text.getText().toString().trim();
		if(TextUtils.isEmpty(text)){
			showTip("�ϳ����ݲ���Ϊ��");
			return;
		}
		setParam();
		int code = mTts.startSpeaking(text, mTtsListener);
		if (code != ErrorCode.SUCCESS) {
			if(code == ErrorCode.ERROR_COMPONENT_NOT_INSTALLED){
				//δ��װ����ת����ʾ��װҳ��
				showTip("��Ҫ��װ����");
			}else {
				showTip("�����ϳ�ʧ��,������: " + code);	
			}
		}
	}
	
	public void listen(View view){
		speak_text.setText(null);
		setListenParam();
		int ret = 0;// �������÷���ֵ
		if (isShowDialog) {
			// ��ʾ��д�Ի���
			iatDialog.setListener(recognizerDialogListener);
			iatDialog.show();
			showTip("�뿪ʼ˵��");
		} else {
			// ����ʾ��д�Ի���
			ret = mIat.startListening(mRecoListener);
			if(ret != ErrorCode.SUCCESS){
				showTip("��дʧ��,�����룺" + ret);
			}else {
				showTip("�뿪ʼ˵��");
			}
		}
	}
	
	private void showTip(String string) {
		Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
	}


	
	/**
	 * ��������
	 * @param param
	 * @return 
	 */
	private void setParam(){
		
		//���úϳ�
		mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
		//���÷�����
		mTts.setParameter(SpeechConstant.VOICE_NAME, voicer);
		
		//��������
		mTts.setParameter(SpeechConstant.SPEED, "50");

		//��������
		mTts.setParameter(SpeechConstant.PITCH, "50");

		//��������
		mTts.setParameter(SpeechConstant.VOLUME, "50");
		
		//���ò�������Ƶ������
		mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");
	}
	
	private void setListenParam(){
		//2.������д������������ƴ�Ѷ��MSC API�ֲ�(Android)��SpeechConstant�� 
		mIat.setParameter(SpeechConstant.DOMAIN, "iat"); 
		mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn"); 
		mIat.setParameter(SpeechConstant.ACCENT, "mandarin "); 
	}
	
	
	/**
	 * �ϳɻص�������
	 */
	private SynthesizerListener mTtsListener = new SynthesizerListener() {
		@Override
		public void onSpeakBegin() {
			showTip("��ʼ����");
		}

		@Override
		public void onSpeakPaused() {
			showTip("��ͣ����");
		}

		@Override
		public void onSpeakResumed() {
			showTip("��������");
		}

		@Override
		public void onBufferProgress(int percent, int beginPos, int endPos,
				String info) {
//			mPercentForBuffering = percent;
//			showTip(String.format("�������Ϊ%d%%�����Ž���Ϊ%d%%", mPercentForBuffering, mPercentForPlaying));
		}

		@Override
		public void onSpeakProgress(int percent, int beginPos, int endPos) {
//			mPercentForPlaying = percent;
//			showTip(String.format("�������Ϊ%d%%�����Ž���Ϊ%d%%", mPercentForBuffering, mPercentForPlaying));
		}

		@Override
		public void onCompleted(SpeechError error) {
			if(error == null)
			{
				showTip("�������");
			}
			else if(error != null)
			{
				showTip(error.getPlainDescription(true));
			}
		}
	};
	
	/**
	 * ��дUI������
	 */
	private RecognizerDialogListener recognizerDialogListener=new RecognizerDialogListener(){
		public void onResult(RecognizerResult results, boolean isLast) {
			String text = JsonParser.parseIatResult(results.getResultString());
			speak_text.append(text);
			speak_text.setSelection(speak_text.length());
		}

		/**
		 * ʶ��ص�����.
		 */
		public void onError(SpeechError error) {
			showTip(error.getPlainDescription(true));
		}

	};
	
	
	//��д������  ����ʾ�Ի���
	private RecognizerListener mRecoListener = new RecognizerListener(){ 
		//��д����ص��ӿ�(����Json��ʽ������û��ɲμ���¼)�� 
		//һ������»�ͨ��onResults�ӿڶ�η��ؽ����������ʶ�������Ƕ�ν�����ۼӣ� 
		//���ڽ���Json�Ĵ���ɲμ�MscDemo��JsonParser�ࣻ
		//isLast����trueʱ�Ự������ 
		public void onResult(RecognizerResult results, boolean isLast) {   
			
			String text = JsonParser.parseIatResult(results.getResultString());
			speak_text.append(text);
			speak_text.setSelection(speak_text.length());
		} 
		//�Ự��������ص��ӿ�  
		public void onError(SpeechError error) { 
			//��ȡ����������
			error.getPlainDescription(true); 
		}  
		//��ʼ¼�� 
		public void onBeginOfSpeech() {
			showTip("��д��ʼ");
		}  
		//����ֵ0~30  
		public void onVolumeChanged(int volume){
			
		}  
		//����¼��  
		public void onEndOfSpeech() {
			showTip("��д���");
		}  
		//��չ�ýӿ�  
		public void onEvent(int eventType,int arg1,int arg2,String msg) {
			
		} 
		
	}; 
		
		
		
		
	
	
	
	/**
	 * ���ڻ�������
	 */
//	private InitListener mTtsInitListener = new InitListener() {
//		@Override
//		public void onInit(int code) {
//			if (code == ErrorCode.SUCCESS) {
//				showTip("��ʼ���ɹ�");
//			}			
//		}
//	};
}
