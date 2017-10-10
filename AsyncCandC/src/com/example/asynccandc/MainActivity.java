package com.example.asynccandc;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		final ToggleButton startButton = (ToggleButton)findViewById(R.id.startButton);
		final Switch directionSwitch = (Switch)findViewById(R.id.directionButton);
		final TextView directionText = (TextView)findViewById(R.id.directionText);
		final SeekBar speedSlider = (SeekBar)findViewById(R.id.speedSlider);
		final EditText speedEdit = (EditText)findViewById(R.id.speedEdit);
		final ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);
		
		directionSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) 
					directionText.setText(R.string.right);
				else
					directionText.setText(R.string.left);
			}
		});
		
		speedEdit.addTextChangedListener(new TextWatcher() {
			int lastVal = -1;
			
			@Override
			public void afterTextChanged(Editable e) {
				String val = e.toString();
				int v;
				try {
					v = Integer.parseInt(val);
				} catch (NumberFormatException x) {
					if (val.length() == 0) return;
					v = lastVal;
				}
				int max = speedSlider.getMax();
				if (v > max) v = max;
				if (v < 0) v = 0;
				String newVal = String.valueOf(v);
				if (!newVal.equals(val)) {
					// Normalized and clipped value
					e.replace(0,  e.length(), newVal);
					return;
				}
				if (v != lastVal) {
					lastVal = v;
					speedSlider.setProgress(v);					
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int before, int count) {}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
		});
		
		speedSlider.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar v, int progress, boolean fromUser) {
				if (fromUser) speedEdit.setText(String.valueOf(progress));
			}

			@Override
			public void onStartTrackingTouch(SeekBar v) { }

			@Override
			public void onStopTrackingTouch(SeekBar v) { }
			
		});
		
		// Now start the AsyncTask to make progress a bar at the given speed 
		// and direction when we are started
		
		startButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			AsyncTask<Void, Integer, Void> asyncTask = null;
			
			@Override
			public void onCheckedChanged(CompoundButton v, boolean checked) {
				if (checked) {
					asyncTask = new AsyncTask<Void, Integer, Void> () {
			
						@Override
						protected Void doInBackground(Void... params) {
							int max = progressBar.getMax() * 100;
							int pos = progressBar.getProgress() * 100;
							while (!isCancelled()) {
								int step = speedSlider.getProgress();
								int end = 0;
								if (directionSwitch.isChecked()) {
									end = max;
								} else {
									step = -step;
								}
								if (pos == end) break;
								pos += step;
								if (pos < 0) pos = 0;
								if (pos > max) pos = max;
								publishProgress(pos/100);
								try {
									Thread.sleep(10);
								} catch(InterruptedException e) {
									asyncTask = null;
									return null;
								}
							}
							asyncTask = null;
							return null;
						}
						
						@Override
						public void onProgressUpdate(Integer... progress) {
							progressBar.setProgress(progress[0]);
						}
						
						@Override
						public void onPostExecute(Void v) {
							startButton.setChecked(false);
							asyncTask = null;
						}
					};
					asyncTask.execute();
				} else if (asyncTask != null) {
					asyncTask.cancel(true);
					asyncTask = null;
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
