package com.istl.samples.faceverification.gui;

import com.istl.samples.faceverification.R;
import com.istl.samples.faceverification.NFV;
import com.neurotec.face.verification.client.NFaceVerificationClient;
import com.neurotec.face.verification.client.NIcaoWarnings;
import com.neurotec.face.verification.client.NLivenessMode;
import com.neurotec.face.verification.client.NVideoFormat;
import com.neurotec.face.verification.server.rest.ApiClient;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SettingsFragment extends PreferenceFragment {

	public static final String KEY_PREF_LIVENESS_TH = "key_pref_liveness_th";
	public static final String KEY_PREF_QUALITY_TH = "key_pref_quality_th";
	public static final String KEY_PREF_MATCHING_TH = "key_pref_matching_th";
	public static final String KEY_PREF_LIVENESS_MODE = "key_pref_liveness_mode";
	public static final String KEY_PREF_LIVENESS_BLINK_TIMEOUT = "key_pref_liveness_blink_timeout";
	public static final String KEY_PREF_LIVENESS_USE_SEPARATE_BLINK = "key_pref_liveness_use_separate_blink";
	public static final String KEY_PREF_LIVENESS_SEPARATE_BLINK_THRESHOLD = "key_pref_liveness_separate_blink_threshold";
	public static final String KEY_PREF_LIVENESS_SEPARATE_BLINK_HYSTERESIS = "key_pref_liveness_separate_blink_hysteresis";
	public static final String KEY_PREF_CHECK_ICAO = "key_pref_check_icao";
	public static final String KEY_PREF_MANUAL_CAPTURING = "key_pref_manual_capturing";
	public static final String KEY_PREF_SATURATION_TH = "key_pref_saturation_th";
	public static final String KEY_PREF_SHARPNESS_TH = "key_pref_sharpness_th";
	public static final String KEY_PREF_BACKGROUD_UNIFORMITY_TH = "key_pref_background_uniformity_th";
	public static final String KEY_PREF_GRAYSCALE_DENSITY_TH = "key_pref_grayscale_density_th";
	public static final String KEY_PREF_LOOKING_AWAY_TH = "key_pref_looking_away_th";
	public static final String KEY_PREF_RED_EYE_TH = "key_pref_red_eye_th";
	public static final String KEY_PREF_FACE_DARKNESS_TH = "key_pref_face_darkness_th";
	public static final String KEY_PREF_UNNATURAL_SKIN_TONE_TH = "key_pref_unnatural_skin_tone_th";
	public static final String KEY_PREF_WASHED_OUT_TH = "key_pref_washed_out_th";
	public static final String KEY_PREF_PIXELATION_TH = "key_pref_pixelation_th";
	public static final String KEY_PREF_SKIN_REFLECTION_TH = "key_pref_skin_reflection_th";
	public static final String KEY_PREF_GLASSES_REFLECTION_TH = "key_pref_glasses_reflection_th";
	public static final String KEY_PREF_EXPRESSION_TH = "key_pref_expression_th";
	public static final String KEY_PREF_DARK_GLASSES_TH = "key_pref_dark_glasses_th";
	public static final String KEY_PREF_BLINK_TH = "key_pref_blink_th";
	public static final String KEY_PREF_MOUTH_OPEN_TH = "key_pref_mouth_open_th";
	public static final String KEY_PREF_CAMERA = "key_pref_camera";
	public static final String KEY_PREF_VIDEO_FORMAT = "key_pref_video_format_1";
	public static final String KEY_PREF_AUTHENTICATION_IP = "key_pref_authentication_ip";
	public static final String KEY_PREF_AUTHENTICATION_KEY = "key_pref_authentication_key";
	public static final String KEY_PREF_LIVENESS_CUSTOM_ACTIONS = "key_pref_custom_actions";

	public static final int PREF_LIVENESS_TH_DEFAULT_VALUE = 50;
	public static final int PREF_LIVENESS_BLINK_TIMEOUT_DEFAULT_VALUE = 2;
	public static final int PREF_LIVENESS_SEPARATE_BLINK_THRESHOLD_DEFAULT_VALUE = 50;
	public static final int PREF_LIVENESS_SEPARATE_BLINK_HYSTERESIS_DEFAULT_VALUE = 1;
	public static final int PREF_QUALITY_TH_DEFAULT_VALUE = 50;
	public static final int PREF_MATCHING_TH_DEFAULT_VALUE = 48;
	public static final String PREF_LIVENESS_MODE_DEFAULT_VALUE = "NONE";
	public static final boolean PREF_CHECK_ICAO_DEFAULT_VALUE = false;
	public static final boolean PREF_MANUAL_CAPTURING_DEFAULT_VALUE = false;
	public  static final boolean PREF_LIVENESS_USE_SEPARATE_BLINK_DEFAULT_VALUE = false;
	public static final int PREF_SATURATION_TH_DEFAULT_VALUE = 50;
	public static final int PREF_SHARPNESS_TH_DEFAULT_VALUE = 50;
	public static final int PREF_BACKGROUND_UNIFORMITY_TH_DEFAULT_VALUE = 0;
	public static final int PREF_GRAYSCALE_DENSITY_TH_DEFAULT_VALUE = 50;
	public static final int PREF_LOOKING_AWAY_TH_DEFAULT_VALUE = 50;
	public static final int PREF_RED_EYE_TH_DEFAULT_VALUE = 50;
	public static final int PREF_FACE_DARKNESS_TH_DEFAULT_VALUE = 50;
	public static final int PREF_UNNATURAL_SKIN_TONE_TH_DEFAULT_VALUE = 30;
	public static final int PREF_WASHED_OUT_TH_DEFAULT_VALUE = 50;
	public static final int PREF_PIXELATION_TH_DEFAULT_VALUE = 50;
	public static final int PREF_SKIN_REFLECTION_TH_DEFAULT_VALUE = 30;
	public static final int PREF_GLASSES_REFLECTION_TH_DEFAULT_VALUE = 50;
	public static final int PREF_EXPRESSION_TH_DEFAULT_VALUE = 5;
	public static final int PREF_DARK_GLASSES_TH_DEFAULT_VALUE = 5;
	public static final int PREF_BLINK_TH_DEFAULT_VALUE = 5;
	public static final int PREF_MOUTH_OPEN_TH_DEFAULT_VALUE = 5;
	public static final String PREF_DEFAULT_CAMERA = "Front";
	public static final String PREF_DEFAULT_AUTHENTICATION_IP = "https://faceverification.neurotechnology.com/rs/";
	public static final String PREF_DEFAULT_AUTHENTICATION_KEY = "9tlitadjedrg1emf9e27d0dlkt";
	public static final String PREF_LIVENESS_CUSTOM_ACTIONS_DEFAULT_VALUE = " ";

	private static final boolean DEBUG = false;
	private static final String PREVIEW_SIZE_SEPARATOR = "x";
	private static final String FPS_SEPARATOR = ":";
	private static final String NAME_SEPARATOR = " ";
	private static final String REGEX_FORMAT_PATTERN = "(.+)" + NAME_SEPARATOR + "(\\d+)" + PREVIEW_SIZE_SEPARATOR + "(\\d+)" + FPS_SEPARATOR + "(\\d+)";

	private static boolean mClientUpdateNeeded = true;

	private ListPreference mCameraPreference = null;
	private ListPreference mVideoFormatPreference = null;

	private SharedPreferences.OnSharedPreferenceChangeListener listener =
			new SharedPreferences.OnSharedPreferenceChangeListener() {
				public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
					try {
						switch (key) {
							case KEY_PREF_LIVENESS_MODE:
								String livenessModeStr = sharedPreferences.getString(key, PREF_LIVENESS_MODE_DEFAULT_VALUE);
								NLivenessMode livenessMode = NLivenessMode.valueOf(livenessModeStr);
								NFV.getInstance().setLivenessMode(livenessMode);
								break;
							case KEY_PREF_LIVENESS_CUSTOM_ACTIONS:
								String customActionsStr = sharedPreferences.getString(key, PREF_LIVENESS_CUSTOM_ACTIONS_DEFAULT_VALUE);
//								NFV.getInstance().setLivenessCustomActionSequence(customActionsStr);
								break;
							case KEY_PREF_LIVENESS_TH:
								byte livenessTh = (byte) sharedPreferences.getInt(key, PREF_LIVENESS_TH_DEFAULT_VALUE);
								NFV.getInstance().setLivenessThreshold(livenessTh);
								break;
							case KEY_PREF_LIVENESS_BLINK_TIMEOUT:
								int livenessBlinkTimeout = sharedPreferences.getInt(key, PREF_LIVENESS_BLINK_TIMEOUT_DEFAULT_VALUE);
								NFV.getInstance().setLivenessBlinkTimeout(livenessBlinkTimeout * 1000);
								break;
							case KEY_PREF_LIVENESS_SEPARATE_BLINK_THRESHOLD:
								int livenessSeparateBlinkThreshold = sharedPreferences.getInt(key, PREF_LIVENESS_SEPARATE_BLINK_THRESHOLD_DEFAULT_VALUE);
//								NFV.getInstance().setLivenessSeparateBlinkThreshold(livenessSeparateBlinkThreshold / 100f);
								break;
							case KEY_PREF_LIVENESS_SEPARATE_BLINK_HYSTERESIS:
								int livenessSeparateBlinkHysteresis = sharedPreferences.getInt(key, PREF_LIVENESS_SEPARATE_BLINK_HYSTERESIS_DEFAULT_VALUE);
//								NFV.getInstance().setLivenessSeparateBlinkHysteresis(livenessSeparateBlinkHysteresis / 100f);
								break;
							case KEY_PREF_LIVENESS_USE_SEPARATE_BLINK:
								boolean livenessUseSeparateBlink = sharedPreferences.getBoolean(key, PREF_LIVENESS_USE_SEPARATE_BLINK_DEFAULT_VALUE);
//								NFV.getInstance().setLivenessUseSeparateBlink(livenessUseSeparateBlink);
								break;
							case KEY_PREF_QUALITY_TH:
								byte qualityTh = (byte) sharedPreferences.getInt(key, PREF_QUALITY_TH_DEFAULT_VALUE);
								NFV.getInstance().setQualityThreshold(qualityTh);
								break;
							case KEY_PREF_MATCHING_TH:
								int matchingTh = sharedPreferences.getInt(key, PREF_MATCHING_TH_DEFAULT_VALUE);
								NFV.getInstance().setMatchingThreshold(matchingTh);
								break;
							case KEY_PREF_CHECK_ICAO:
								boolean checkIcao = sharedPreferences.getBoolean(key, PREF_CHECK_ICAO_DEFAULT_VALUE);
								NFV.getInstance().setCheckIcaoCompliance(checkIcao);
								break;
							case KEY_PREF_MANUAL_CAPTURING:
								boolean manual = sharedPreferences.getBoolean(key, PREF_MANUAL_CAPTURING_DEFAULT_VALUE);
								NFV.getInstance().setUseManualExtraction(manual);
								break;
							case KEY_PREF_SATURATION_TH:
								byte saturationTh = (byte) sharedPreferences.getInt(key, PREF_SATURATION_TH_DEFAULT_VALUE);
								NFV.getInstance().setIcaoWarningThreshold(NIcaoWarnings.SATURATION, saturationTh);
								break;
							case KEY_PREF_SHARPNESS_TH:
								byte sharpnessTh = (byte) sharedPreferences.getInt(key, PREF_SHARPNESS_TH_DEFAULT_VALUE);
								NFV.getInstance().setIcaoWarningThreshold(NIcaoWarnings.SHARPNESS, sharpnessTh);
								break;
							case KEY_PREF_BACKGROUD_UNIFORMITY_TH:
								byte backgroundUniformityTh = (byte) sharedPreferences.getInt(key, PREF_BACKGROUND_UNIFORMITY_TH_DEFAULT_VALUE);
								NFV.getInstance().setIcaoWarningThreshold(NIcaoWarnings.BACKGROUND_UNIFORMITY, backgroundUniformityTh);
								break;
							case KEY_PREF_GRAYSCALE_DENSITY_TH:
								byte grayscaleDensityTh = (byte) sharedPreferences.getInt(key, PREF_GRAYSCALE_DENSITY_TH_DEFAULT_VALUE);
								NFV.getInstance().setIcaoWarningThreshold(NIcaoWarnings.GRAYSCALE_DENSITY, grayscaleDensityTh);
								break;
							case KEY_PREF_LOOKING_AWAY_TH:
								byte lookingAwayTh = (byte) sharedPreferences.getInt(key, PREF_LOOKING_AWAY_TH_DEFAULT_VALUE);
								NFV.getInstance().setIcaoWarningThreshold(NIcaoWarnings.LOOKING_AWAY, lookingAwayTh);
								break;
							case KEY_PREF_RED_EYE_TH:
								byte redEyeTh = (byte) sharedPreferences.getInt(key, PREF_RED_EYE_TH_DEFAULT_VALUE);
								NFV.getInstance().setIcaoWarningThreshold(NIcaoWarnings.RED_EYE, redEyeTh);
								break;
							case KEY_PREF_FACE_DARKNESS_TH:
								byte faceDarknessTh = (byte) sharedPreferences.getInt(key, PREF_FACE_DARKNESS_TH_DEFAULT_VALUE);
								NFV.getInstance().setIcaoWarningThreshold(NIcaoWarnings.FACE_DARKNESS, faceDarknessTh);
								break;
							case KEY_PREF_UNNATURAL_SKIN_TONE_TH:
								byte unnaturalSkinToneTh = (byte) sharedPreferences.getInt(key, PREF_UNNATURAL_SKIN_TONE_TH_DEFAULT_VALUE);
								NFV.getInstance().setIcaoWarningThreshold(NIcaoWarnings.UNNATURAL_SKIN_TONE, unnaturalSkinToneTh);
								break;
							case KEY_PREF_WASHED_OUT_TH:
								byte washedOutTh = (byte) sharedPreferences.getInt(key, PREF_WASHED_OUT_TH_DEFAULT_VALUE);
								NFV.getInstance().setIcaoWarningThreshold(NIcaoWarnings.WASHED_OUT, washedOutTh);
								break;
							case KEY_PREF_PIXELATION_TH:
								byte pixelationTh = (byte) sharedPreferences.getInt(key, PREF_PIXELATION_TH_DEFAULT_VALUE);
								NFV.getInstance().setIcaoWarningThreshold(NIcaoWarnings.PIXELATION, pixelationTh);
								break;
							case KEY_PREF_SKIN_REFLECTION_TH:
								byte skinReflectionTh = (byte) sharedPreferences.getInt(key, PREF_SKIN_REFLECTION_TH_DEFAULT_VALUE);
								NFV.getInstance().setIcaoWarningThreshold(NIcaoWarnings.SKIN_REFLECTION, skinReflectionTh);
								break;
							case KEY_PREF_GLASSES_REFLECTION_TH:
								byte glassesReflectionTh = (byte) sharedPreferences.getInt(key, PREF_GLASSES_REFLECTION_TH_DEFAULT_VALUE);
								NFV.getInstance().setIcaoWarningThreshold(NIcaoWarnings.GLASSES_REFLECTION, glassesReflectionTh);
								break;
							case KEY_PREF_EXPRESSION_TH:
								byte expressionTh = (byte) sharedPreferences.getInt(key, PREF_EXPRESSION_TH_DEFAULT_VALUE);
								NFV.getInstance().setIcaoWarningThreshold(NIcaoWarnings.EXPRESSION, expressionTh);
								break;
							case KEY_PREF_DARK_GLASSES_TH:
								byte darkGlassesTh = (byte) sharedPreferences.getInt(key, PREF_DARK_GLASSES_TH_DEFAULT_VALUE);
								NFV.getInstance().setIcaoWarningThreshold(NIcaoWarnings.DARK_GLASSES, darkGlassesTh);
								break;
							case KEY_PREF_BLINK_TH:
								byte blinkTh = (byte) sharedPreferences.getInt(key, PREF_BLINK_TH_DEFAULT_VALUE);
								NFV.getInstance().setIcaoWarningThreshold(NIcaoWarnings.BLINK, blinkTh);
								break;
							case KEY_PREF_MOUTH_OPEN_TH:
								byte mouthOpenTh = (byte) sharedPreferences.getInt(key, PREF_MOUTH_OPEN_TH_DEFAULT_VALUE);
								NFV.getInstance().setIcaoWarningThreshold(NIcaoWarnings.MOUTH_OPEN, mouthOpenTh);
								break;
							case KEY_PREF_CAMERA:
								updateCamera(NFV.getInstance(), sharedPreferences);
							case KEY_PREF_VIDEO_FORMAT:
								updateVideoFormat(NFV.getInstance(), sharedPreferences);
								break;
							case KEY_PREF_AUTHENTICATION_IP:
							case KEY_PREF_AUTHENTICATION_KEY:
								mClientUpdateNeeded = true;
								break;
						}
					} catch (Exception exception) {
						ErrorDialogFragment.newInstance(exception.getMessage(), false).show(getFragmentManager(), "error");
					}
				}
			};

	public static synchronized boolean isUpdateClientNeeded() {
		return mClientUpdateNeeded;
	}

	public static synchronized void updateClientAuthentification(ApiClient client) {
		try {
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(NFaceVerificationClient.getContext());
			String path = sharedPreferences.getString(KEY_PREF_AUTHENTICATION_IP, PREF_DEFAULT_AUTHENTICATION_IP);
			if (DEBUG) Log.i("TEST", "Path: " + path);
			client.setBasePath(path);
			String key = sharedPreferences.getString(KEY_PREF_AUTHENTICATION_KEY, PREF_DEFAULT_AUTHENTICATION_KEY);
			if (DEBUG) Log.i("TEST", "Key: " + key);
			client.setApiKey(key);
			if (DEBUG) Log.i("TEST", "Key++: " + key);
			mClientUpdateNeeded = false;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static void loadSettings() {
		NFaceVerificationClient instance = NFV.getInstance();
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(NFaceVerificationClient.getContext());

		String value = sharedPreferences.getString(KEY_PREF_LIVENESS_MODE, PREF_LIVENESS_MODE_DEFAULT_VALUE);
		NLivenessMode livenessMode = NLivenessMode.valueOf(value);
		instance.setLivenessMode(livenessMode);

		value = sharedPreferences.getString(KEY_PREF_LIVENESS_CUSTOM_ACTIONS, PREF_LIVENESS_CUSTOM_ACTIONS_DEFAULT_VALUE);
//		instance.setLivenessCustomActionSequence(value);

		byte byteTh = (byte) sharedPreferences.getInt(KEY_PREF_LIVENESS_TH, PREF_LIVENESS_TH_DEFAULT_VALUE);
		instance.setLivenessThreshold(byteTh);

		byteTh = (byte) sharedPreferences.getInt(KEY_PREF_QUALITY_TH, PREF_QUALITY_TH_DEFAULT_VALUE);
		instance.setQualityThreshold(byteTh);

		byteTh = (byte) sharedPreferences.getInt(KEY_PREF_SATURATION_TH, PREF_SATURATION_TH_DEFAULT_VALUE);
		instance.setIcaoWarningThreshold(NIcaoWarnings.SATURATION, byteTh);
		byteTh = (byte) sharedPreferences.getInt(KEY_PREF_SHARPNESS_TH, PREF_SHARPNESS_TH_DEFAULT_VALUE);
		instance.setIcaoWarningThreshold(NIcaoWarnings.SHARPNESS, byteTh);
		byteTh = (byte) sharedPreferences.getInt(KEY_PREF_BACKGROUD_UNIFORMITY_TH, PREF_BACKGROUND_UNIFORMITY_TH_DEFAULT_VALUE);
		instance.setIcaoWarningThreshold(NIcaoWarnings.BACKGROUND_UNIFORMITY, byteTh);
		byteTh = (byte) sharedPreferences.getInt(KEY_PREF_GRAYSCALE_DENSITY_TH, PREF_GRAYSCALE_DENSITY_TH_DEFAULT_VALUE);
		instance.setIcaoWarningThreshold(NIcaoWarnings.GRAYSCALE_DENSITY, byteTh);
		byteTh = (byte) sharedPreferences.getInt(KEY_PREF_LOOKING_AWAY_TH, PREF_LOOKING_AWAY_TH_DEFAULT_VALUE);
		instance.setIcaoWarningThreshold(NIcaoWarnings.LOOKING_AWAY, byteTh);
		byteTh = (byte) sharedPreferences.getInt(KEY_PREF_RED_EYE_TH, PREF_RED_EYE_TH_DEFAULT_VALUE);
		instance.setIcaoWarningThreshold(NIcaoWarnings.RED_EYE, byteTh);
		byteTh = (byte) sharedPreferences.getInt(KEY_PREF_FACE_DARKNESS_TH, PREF_FACE_DARKNESS_TH_DEFAULT_VALUE);
		instance.setIcaoWarningThreshold(NIcaoWarnings.FACE_DARKNESS, byteTh);
		byteTh = (byte) sharedPreferences.getInt(KEY_PREF_UNNATURAL_SKIN_TONE_TH, PREF_UNNATURAL_SKIN_TONE_TH_DEFAULT_VALUE);
		instance.setIcaoWarningThreshold(NIcaoWarnings.UNNATURAL_SKIN_TONE, byteTh);
		byteTh = (byte) sharedPreferences.getInt(KEY_PREF_WASHED_OUT_TH, PREF_WASHED_OUT_TH_DEFAULT_VALUE);
		instance.setIcaoWarningThreshold(NIcaoWarnings.WASHED_OUT, byteTh);
		byteTh = (byte) sharedPreferences.getInt(KEY_PREF_PIXELATION_TH, PREF_PIXELATION_TH_DEFAULT_VALUE);
		instance.setIcaoWarningThreshold(NIcaoWarnings.PIXELATION, byteTh);
		byteTh = (byte) sharedPreferences.getInt(KEY_PREF_SKIN_REFLECTION_TH, PREF_SKIN_REFLECTION_TH_DEFAULT_VALUE);
		instance.setIcaoWarningThreshold(NIcaoWarnings.SKIN_REFLECTION, byteTh);
		byteTh = (byte) sharedPreferences.getInt(KEY_PREF_GLASSES_REFLECTION_TH, PREF_GLASSES_REFLECTION_TH_DEFAULT_VALUE);
		instance.setIcaoWarningThreshold(NIcaoWarnings.GLASSES_REFLECTION, byteTh);
		byteTh = (byte) sharedPreferences.getInt(KEY_PREF_EXPRESSION_TH, PREF_EXPRESSION_TH_DEFAULT_VALUE);
		instance.setIcaoWarningThreshold(NIcaoWarnings.EXPRESSION, byteTh);
		byteTh = (byte) sharedPreferences.getInt(KEY_PREF_DARK_GLASSES_TH, PREF_DARK_GLASSES_TH_DEFAULT_VALUE);
		instance.setIcaoWarningThreshold(NIcaoWarnings.DARK_GLASSES, byteTh);
		byteTh = (byte) sharedPreferences.getInt(KEY_PREF_BLINK_TH, PREF_BLINK_TH_DEFAULT_VALUE);
		instance.setIcaoWarningThreshold(NIcaoWarnings.BLINK, byteTh);
		byteTh = (byte) sharedPreferences.getInt(KEY_PREF_MOUTH_OPEN_TH, PREF_MOUTH_OPEN_TH_DEFAULT_VALUE);
		instance.setIcaoWarningThreshold(NIcaoWarnings.MOUTH_OPEN, byteTh);

		int intTh = sharedPreferences.getInt(KEY_PREF_MATCHING_TH, PREF_MATCHING_TH_DEFAULT_VALUE);
		instance.setMatchingThreshold(intTh);
		intTh = sharedPreferences.getInt(KEY_PREF_LIVENESS_BLINK_TIMEOUT, PREF_LIVENESS_BLINK_TIMEOUT_DEFAULT_VALUE);
		instance.setLivenessBlinkTimeout(intTh * 1000);
		intTh = sharedPreferences.getInt(KEY_PREF_LIVENESS_SEPARATE_BLINK_THRESHOLD, PREF_LIVENESS_SEPARATE_BLINK_THRESHOLD_DEFAULT_VALUE);
//		instance.setLivenessSeparateBlinkThreshold(intTh / 100f);
		intTh = sharedPreferences.getInt(KEY_PREF_LIVENESS_SEPARATE_BLINK_HYSTERESIS, PREF_LIVENESS_SEPARATE_BLINK_HYSTERESIS_DEFAULT_VALUE);
//		instance.setLivenessSeparateBlinkHysteresis(intTh / 100f);

		boolean boolValue = sharedPreferences.getBoolean(KEY_PREF_CHECK_ICAO, PREF_CHECK_ICAO_DEFAULT_VALUE);
		instance.setCheckIcaoCompliance(boolValue);
		boolValue = sharedPreferences.getBoolean(KEY_PREF_MANUAL_CAPTURING, PREF_MANUAL_CAPTURING_DEFAULT_VALUE);
		instance.setUseManualExtraction(boolValue);
		boolValue = sharedPreferences.getBoolean(KEY_PREF_LIVENESS_USE_SEPARATE_BLINK, PREF_LIVENESS_USE_SEPARATE_BLINK_DEFAULT_VALUE);
//		instance.setLivenessUseSeparateBlink(boolValue);

		updateCamera(instance, sharedPreferences);
		updateVideoFormat(instance, sharedPreferences);
	}

	private static void updateCamera(NFaceVerificationClient instance, SharedPreferences sharedPreferences) {
		String camera = sharedPreferences.getString(KEY_PREF_CAMERA, PREF_DEFAULT_CAMERA);
		String[] cameras = instance.getAvailableCameraNames();
		for (String n : cameras) {
			if (n.contains(camera)) {
				if (DEBUG) Log.i("TEST", "Setting camera: " + n);
				instance.setCurrentCamera(n);
				break;
			}
		}
	}

	private static void updateVideoFormat(NFaceVerificationClient instance, SharedPreferences sharedPreferences) {
		String videoFormat = sharedPreferences.getString(KEY_PREF_VIDEO_FORMAT, videoFormatToString(instance.getCurrentVideoFormat()));
		if (DEBUG) Log.i("TEST", "videoFormat: " + videoFormat);
		if (DEBUG) Log.i("TEST", "currentVideoFormat: " + videoFormatToString(instance.getCurrentVideoFormat()));
		boolean validFormat = false;
		Object[] formatValues = stringToVideoFormatValues(videoFormat);
		for (NVideoFormat format : instance.getAvailableVideoFormats()) {
				if ((format.getMediaSubTypeAsString().equals(formatValues[0])
					&& format.getWidth() == (int)formatValues[1])
					&& (format.getHeight() == (int)formatValues[2])
					&& (format.getFrameRate()[0] == (int)formatValues[3])) {
				instance.setCurrentVideoFormat(format);
				if (DEBUG) Log.i("TEST", "Setting format n: " + format.getMediaSubTypeAsString()
						+ " w: " + format.getWidth() + " h: " + format.getHeight() + " fps: " + format.getFrameRate()[0]);
				validFormat = true;
				break;
			}
		}

		if (!validFormat) {
			if (DEBUG) Log.i("TEST", "not valid setting: " + videoFormatToString(instance.getCurrentVideoFormat()) + " " + instance.getCurrentVideoFormat().getMediaSubType());
			sharedPreferences.edit().putString(KEY_PREF_VIDEO_FORMAT, videoFormatToString(instance.getCurrentVideoFormat())).commit();
		}
	}

	private static String videoFormatToString(NVideoFormat format) {
		StringBuilder sb = new StringBuilder();
		sb.append(format.getMediaSubTypeAsString());
		sb.append(NAME_SEPARATOR);
		sb.append(format.getWidth());
		sb.append(PREVIEW_SIZE_SEPARATOR);
		sb.append(format.getHeight());
		sb.append(FPS_SEPARATOR);
		sb.append(format.getFrameRate()[0]);
		return sb.toString();
	}

	private static Object[] stringToVideoFormatValues(String vformat) {
		Pattern pattern = Pattern.compile(REGEX_FORMAT_PATTERN);
		Matcher matcher = pattern.matcher(vformat);
		if (matcher.find()) {
			Object[] result = new Object[4];
			result[0] = matcher.group(1); // name
			result[1] = Integer.parseInt(matcher.group(2)); // width
			result[2] = Integer.parseInt(matcher.group(3)); // height
			result[3] = Integer.parseInt(matcher.group(4)); // fps
			return result;
		} else {
			throw new IllegalArgumentException("Unable to parse video format " + vformat);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		NFaceVerificationClient nfv = NFV.getInstance();
		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);

		mCameraPreference = (ListPreference) findPreference(KEY_PREF_CAMERA);
		String[] cameras = nfv.getAvailableCameraNames();
		mCameraPreference.setEntries(cameras);
		mCameraPreference.setEntryValues(cameras);

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(NFaceVerificationClient.getContext());
		String camera = sharedPreferences.getString(KEY_PREF_CAMERA, PREF_DEFAULT_CAMERA);
		for (String n : cameras) {
			if (n.contains(camera)) {
				if (DEBUG) Log.i("TEST", "Setting camera: " + n);
				mCameraPreference.setValue(n);
				break;
			}
		}

		mVideoFormatPreference = (ListPreference) findPreference(KEY_PREF_VIDEO_FORMAT);

		List<String> availableVideoFormats = new ArrayList<String>();
		for (NVideoFormat format : nfv.getAvailableVideoFormats()) {
			availableVideoFormats.add(videoFormatToString(format));
		}

		String[] availableVideoFormatArray = availableVideoFormats.toArray(new String[availableVideoFormats.size()]);

		mVideoFormatPreference.setEntries(availableVideoFormatArray);
		mVideoFormatPreference.setEntryValues(availableVideoFormatArray);

		String videoFormat = sharedPreferences.getString(KEY_PREF_VIDEO_FORMAT, videoFormatToString(nfv.getCurrentVideoFormat()));
		mVideoFormatPreference.setValue(videoFormat);
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	@Override
	public void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
	}

	@Override
	public void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener);
	}


}