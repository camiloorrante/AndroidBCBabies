package com.neoris.bcbabies;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.integratedbiometrics.ibscanultimate.IBScan;
import com.integratedbiometrics.ibscanultimate.IBScanDevice;
import com.integratedbiometrics.ibscanultimate.IBScanDeviceListener;
import com.integratedbiometrics.ibscanultimate.IBScanException;
import com.integratedbiometrics.ibscanultimate.IBScanListener;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.io.ByteArrayOutputStream;

public class SecondFragment extends Fragment implements
        IBScanListener, IBScanDeviceListener {
    Button button;
    Button button2;
    EditText etxNewBornName;
    RadioGroup sexRadioGroup;
    RadioButton rbMale, rbFemale;
    String fingerPrintHash;

    OnPauseListener onPauseListener;

    public interface OnPauseListener {
        void onSecondFragmentPause(String newBornName, String fingerPrintHash, Integer gender);
    }

    //region IBScan Declaraciones
    /* *********************************************************************************************
     * CONSTANTES PRIVADAS
     ******************************************************************************************** */

    /* Tag utilizada para los mensajes de registro de Android desde esta aplicación. */
    private static final String TAG = "Simple Scan";

    protected static final int __INVALID_POS__ = -1;

    /*El valor predeterminado del estado TextView. */
    protected static final String __NFIQ_DEFAULT__ = "0-0-0-0";

    /* El valor predeterminado del tiempo de marco TextView. */
    protected static final String __NA_DEFAULT__ = "n/a";

    /* El nombre de archivo predeterminado para imágenes y plantillas para correo electrónico. */
    protected static final String FILE_NAME_DEFAULT = "output";

    /* La cantidad de calidades de dedos establecida en la imagen de vista previa. */
    protected static final int FINGER_QUALITIES_COUNT = 4;

    /* El color de fondo de la imagen de vista previa ImageView. */
    protected static final int PREVIEW_IMAGE_BACKGROUND = Color.LTGRAY;

    protected final int __TIMER_STATUS_DELAY__ = 500;

    // Definiciones de secuencias de captura
    protected final String CAPTURE_SEQ_FLAT_SINGLE_FINGER = "Single flat finger";
    protected final String CAPTURE_SEQ_ROLL_SINGLE_FINGER = "Single rolled finger";
    protected final String CAPTURE_SEQ_2_FLAT_FINGERS = "2 flat fingers";
    protected final String CAPTURE_SEQ_10_SINGLE_FLAT_FINGERS = "10 single flat fingers";
    protected final String CAPTURE_SEQ_10_SINGLE_ROLLED_FINGERS = "10 single rolled fingers";
    protected final String CAPTURE_SEQ_4_FLAT_FINGERS = "4 flat fingers";
    protected final String CAPTURE_SEQ_10_FLAT_WITH_4_FINGER_SCANNER = "10 flat fingers with 4-finger scanner";

    // Definiciones de pitidos
    protected final int __BEEP_FAIL__ = 0;
    protected final int __BEEP_SUCCESS__ = 1;
    protected final int __BEEP_OK__ = 2;
    protected final int __BEEP_DEVICE_COMMUNICATION_BREAK__ = 3;

    // Definiciones de color LED
    protected final int __LED_COLOR_NONE__ = 0;
    protected final int __LED_COLOR_GREEN__ = 1;
    protected final int __LED_COLOR_RED__ = 2;
    protected final int __LED_COLOR_YELLOW__ = 3;

    // Definiciones de teclas
    protected final int __LEFT_KEY_BUTTON__ = 1;
    protected final int __RIGHT_KEY_BUTTON__ = 2;

    /* La cantidad de segmentos de dedo establecidos en la imagen de resultado. */
    protected static final int FINGER_SEGMENT_COUNT = 4;

    /* *********************************************************************************************
     * CLASES PRIVADAS
     ******************************************************************************************** */

    /*
     * Esta clase ajusta los datos guardados por la aplicación para cambios de configuración.
     */
    protected class AppData {
        /* El dispositivo usb actualmente seleccionado. */
        public int usbDevices = __INVALID_POS__;

        /* La secuencia de captura actualmente seleccionada. */
        public int captureSeq = __INVALID_POS__;

        /* El contenido actual de nfiq TextView. */
        public String nfiq = __NFIQ_DEFAULT__;

        /* El contenido actual del marco de tiempo TextView. */
        public String frameTime = __NA_DEFAULT__;

        /* La imagen actual que se muestra en la vista previa de la imagen ImageView. */
        public Bitmap imageBitmap = null;

        /* Indica si se puede hacer clic largo en la vista previa de la imagen ImageView. */
        public boolean imagePreviewImageClickable = false;

        /* El contenido actual de la superposición Texto TextView. */
        public String overlayText = "";

        /* El contenido actual del color de superposición para overlayText TextView. */
        public int overlayColor = PREVIEW_IMAGE_BACKGROUND;

        /* El contenido actual del mensaje de estado TextView. */
        public String statusMessage = __NA_DEFAULT__;
    }

    protected class CaptureInfo {
        String PreCaptureMessage;        // para mostrar en la ventana de huellas dactilares
        String PostCaptuerMessage;        // para mostrar en la ventana de huellas dactilares
        IBScanDevice.ImageType ImageType;                // modo de captura
        int NumberOfFinger;            // número de conteo de dedos
        String fingerName;                // nombre del dedo (por ejemplo, pulgares izquierdo, índice izquierdo ...)
    }

    ;

    /* *********************************************************************************************
     * CAMPOS PRIVADOS (COMPONENTES DE LA IU)
     ******************************************************************************************** */

    private TextView m_txtStatusMessage;
    private ImageView m_imgPreview;
    private Spinner m_cboUsbDevices;
    private Button m_btnCaptureStart;
    private Button m_btnCaptureStop;
    private Dialog m_enlargedDialog;
    private Bitmap m_BitmapImage;

    /* *********************************************************************************************
     * CAMPOS PRIVADOS
     ******************************************************************************************** */

    /*
     Un identificador para la instancia única de la clase IBScan que será la interfaz principal}
     de la biblioteca, para operaciones como obtener el número de escáneres (getDeviceCount ())
     y abrir escáneres (openDeviceAsync ()).
     */
    private IBScan m_ibScan;

    /*
     Un identificador para el IBScanDevice abierto (si lo hay) que será la interfaz
     para obtener datos del escáner abierto, incluida la captura de la imagen (beginCaptureImage (),
     cancelCaptureImage ()) y el tipo de imagen que se captura.
     */
    private IBScanDevice m_ibScanDevice;

    /*
     *Un objeto que reproducirá un sonido cuando la captura de la imagen se haya completado.
     */
    private PlaySound m_beeper = new PlaySound();

    /*
     * Información retenida para mostrar vista.
     */
    private IBScanDevice.ImageData m_lastResultImage;
    private IBScanDevice.ImageData[] m_lastSegmentImages = new IBScanDevice.ImageData[FINGER_SEGMENT_COUNT];

    /*
     * Información retenida para cambios de orientación.
     */
    private SecondFragment.AppData m_savedData = new SecondFragment.AppData();

    protected int m_nSelectedDevIndex = -1;                ///< Índice del dispositivo seleccionado
    protected boolean m_bInitializing = false;                ///< La inicialización del dispositivo está en progreso
    protected String m_ImgSaveFolderName = "";
    String m_ImgSaveFolder = "";                    ///< Carpeta base para guardar imágenes
    String m_ImgSubFolder = "";                    ///< Sub Carpeta para secuencia de imágenes
    protected String m_strImageMessage = "";
    protected boolean m_bNeedClearPlaten = false;
    protected boolean m_bBlank = false;
    protected boolean m_bSaveWarningOfClearPlaten;

    protected Vector<SecondFragment.CaptureInfo> m_vecCaptureSeq = new Vector<SecondFragment.CaptureInfo>();        ///< Secuencia de pasos de captura
    protected int m_nCurrentCaptureStep = -1;                    ///< Paso de captura actual

    protected IBScanDevice.LedState m_LedState;
    protected IBScanDevice.FingerQualityState[] m_FingerQuality = {IBScanDevice.FingerQualityState.FINGER_NOT_PRESENT, IBScanDevice.FingerQualityState.FINGER_NOT_PRESENT, IBScanDevice.FingerQualityState.FINGER_NOT_PRESENT, IBScanDevice.FingerQualityState.FINGER_NOT_PRESENT};
    protected IBScanDevice.ImageType m_ImageType;
    protected int m_nSegmentImageArrayCount = 0;
    protected IBScanDevice.SegmentPosition[] m_SegmentPositionArray;

    protected ArrayList<String> m_arrUsbDevices;
    protected ArrayList<String> m_arrCaptureSeq;

    protected byte[] m_drawBuffer;
    protected double m_scaleFactor;
    protected int m_leftMargin;
    protected int m_topMargin;


    // ////////////////////////////////////////////////////////////////////////////////////////////////////
    // GLobal varias definiciones
    // ////////////////////////////////////////////////////////////////////////////////////////////////////

    //endregion termina las declaraciones del IBScan de
    public SecondFragment() {
        // Required empty public constructor
    }

    @Override
    public void onPause() {
        super.onPause();
        int selectedSexId = sexRadioGroup.getCheckedRadioButtonId();
        Integer sex;
        if (rbMale.getId() == selectedSexId){
            sex = 1;
        }
        else {
            sex = 0;
        }
        onPauseListener.onSecondFragmentPause(etxNewBornName.getText().toString(), fingerPrintHash, sex);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        Activity activity;
        if (context instanceof Activity)
            activity = (Activity) context;
        else
            activity = null;

        try{
            onPauseListener = (SecondFragment.OnPauseListener)activity;
        }
        catch (ClassCastException ex){
            throw new ClassCastException(activity.toString() + "must implement OnPauseListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View RootView = inflater.inflate(R.layout.fragment_second, container, false);


        m_txtStatusMessage = (TextView) RootView.findViewById(R.id.txtStatusMessage);
        m_imgPreview = (ImageView) RootView.findViewById(R.id.imgPreview);
        m_imgPreview.setBackgroundColor(PREVIEW_IMAGE_BACKGROUND);

        m_btnCaptureStop = (Button) RootView.findViewById(R.id.stop_capture_btn);
        m_btnCaptureStop.setOnClickListener(this.m_btnCaptureStopClickListener);

        m_btnCaptureStart = (Button) RootView.findViewById(R.id.start_capture_btn);
        m_btnCaptureStart.setOnClickListener(this.m_btnCaptureStartClickListener);

        etxNewBornName = (EditText) RootView.findViewById(R.id.enewBornName);
        m_cboUsbDevices = (Spinner) RootView.findViewById(R.id.spinUsbDevices);
        sexRadioGroup = (RadioGroup) RootView.findViewById(R.id.rgSex);
        rbMale = (RadioButton) RootView.findViewById(R.id.rbMale);
        rbFemale = (RadioButton) RootView.findViewById(R.id.rbFemale);
        /* */
        //region Inicializaciones de IBScan
        m_ibScan = IBScan.getInstance(getActivity().getApplicationContext());
        m_ibScan.setScanListener(this);

        Resources r = Resources.getSystem();
        Configuration config = r.getConfiguration();
        /*
         Asegúrese de que no haya dispositivos USB conectados que sean escáneres IB
         para los que no se haya otorgado el permiso. Para cualquiera que se encuentre,
         solicite permiso; deberíamos recibir una devolución de llamada cuando se conceda
         o deniegue el permiso y luego cuando IBScan reconozca que los nuevos dispositivos
         están conectados, lo que dará como resultado otra actualización.
         */

        final UsbManager manager = (UsbManager) getActivity().getApplicationContext().getSystemService(Context.USB_SERVICE);
        final HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        final Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while (deviceIterator.hasNext()) {
            final UsbDevice device = deviceIterator.next();
            final boolean isScanDevice = IBScan.isScanDevice(device);
            if (isScanDevice) {
                final boolean hasPermission = manager.hasPermission(device);
                if (!hasPermission) {
                    this.m_ibScan.requestPermission(device.getDeviceId());
                }
            }
        }

        OnMsg_UpdateDeviceList(false);

        /* Inicializar la IU con datos. */
        _PopulateUI();

        _TimerTaskThreadCallback thread = new _TimerTaskThreadCallback(__TIMER_STATUS_DELAY__);
        thread.start();
        //endregion terminan inicializaciones del IBScan


        return RootView;
    }


    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        String item = parent.getItemAtPosition(position).toString();

    }
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

    //region IBScan methods
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        //setContentView(R.layout.ib_scan_port);


        /* Inicializar campos de IU para una nueva orientación. */
        _InitUIFields(getView());

        OnMsg_UpdateDeviceList(true);

        /* Rellene la interfaz de usuario con datos de orientación anterior. */
        _PopulateUI();

    }

    /*
     * Libere los recursos del controlador.
     */

    @Override
    public void onDestroy() {
        super.onDestroy();

        for (int i = 0; i < 10; i++) {
            try {
                _ReleaseDevice();
                break;
            } catch (IBScanException ibse) {
                if (ibse.getType().equals(IBScanException.Type.RESOURCE_LOCKED)) {
                } else {
                    break;
                }
            }
        }
    }

//    @Override
//    public void onBackPressed() {
//        exitApp(getActivity());
//    }


    /* *********************************************************************************************
     * MÉTODOS PRIVADOS
     ******************************************************************************************** */

    /*
     * Inicializa los campos de la interfaz de usuario para una nueva orientación.
     */
    private void _InitUIFields(View rootView) {
        m_txtStatusMessage = (TextView) rootView.findViewById(R.id.txtStatusMessage);

        m_imgPreview = (ImageView) rootView.findViewById(R.id.imgPreview);
        m_imgPreview.setBackgroundColor(PREVIEW_IMAGE_BACKGROUND);

        m_btnCaptureStop = (Button) rootView.findViewById(R.id.stop_capture_btn);
        m_btnCaptureStop.setOnClickListener(this.m_btnCaptureStopClickListener);

        m_btnCaptureStart = (Button) rootView.findViewById(R.id.start_capture_btn);
        m_btnCaptureStart.setOnClickListener(this.m_btnCaptureStartClickListener);

        m_cboUsbDevices = (Spinner) rootView.findViewById(R.id.spinUsbDevices);
    }

    /*
     * Rellene la interfaz de usuario con datos de orientación anterior.
     */
    private void _PopulateUI() {

        if (m_savedData.usbDevices != __INVALID_POS__) {
            m_cboUsbDevices.setSelection(m_savedData.usbDevices);
        }

        if (m_savedData.imageBitmap != null) {
            m_imgPreview.setImageBitmap(m_savedData.imageBitmap);
        }

        if (m_BitmapImage != null) {
            m_BitmapImage.isRecycled();
        }

        m_imgPreview.setLongClickable(m_savedData.imagePreviewImageClickable);
    }

    // Get IBScan.
    protected IBScan getIBScan() {
        return (this.m_ibScan);
    }

    // Get opened or null IBScanDevice.
    protected IBScanDevice getIBScanDevice() {
        return (this.m_ibScanDevice);
    }

    // Set IBScanDevice.
    protected void setIBScanDevice(IBScanDevice ibScanDevice) {
        m_ibScanDevice = ibScanDevice;
        if (ibScanDevice != null) {
            ibScanDevice.setScanDeviceListener(this);
        }
    }

    /*
     * Set status message text box.
     */
    protected void _SetStatusBarMessage(final String s) {
        /* Asegúrese de que esto ocurra en el hilo de UI. */
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                m_txtStatusMessage.setText(s);
            }
        });
    }

    /*
     * Timer task usando un Thread
     */
    class _TimerTaskThreadCallback extends Thread {
        private int timeInterval;

        _TimerTaskThreadCallback(int timeInterval) {
            this.timeInterval = timeInterval;
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                if (getIBScanDevice() != null) {
                    OnMsg_DrawFingerQuality();

                    if (m_bNeedClearPlaten)
                        m_bBlank = !m_bBlank;
                }

                _Sleep(timeInterval);

                try {
                    Thread.sleep(timeInterval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*
     * Inicializar el dispositivo usando un hilo
     */
    class _InitializeDeviceThreadCallback extends Thread {
        private int devIndex;

        _InitializeDeviceThreadCallback(int devIndex) {
            this.devIndex = devIndex;
        }

        @Override
        public void run() {
            try {
                m_bInitializing = true;
                IBScanDevice ibScanDeviceNew = getIBScan().openDevice(this.devIndex);
                setIBScanDevice(ibScanDeviceNew);
                m_bInitializing = false;

                if (ibScanDeviceNew != null) {
                    //getProperty device Width,Height
/*					String imageW = getIBScanDevice().getProperty(PropertyId.IMAGE_WIDTH);
					String imageH = getIBScanDevice().getProperty(PropertyId.IMAGE_HEIGHT);
					int	imageWidth = Integer.parseInt(imageW);
					int	imageHeight = Integer.parseInt(imageH);
//					m_BitmapImage = _CreateBitmap(imageWidth, imageHeight);
*/
                    int outWidth = m_imgPreview.getWidth() - 20;
                    int outHeight = m_imgPreview.getHeight() - 20;
                    m_BitmapImage = Bitmap.createBitmap(outWidth, outHeight, Bitmap.Config.ARGB_8888);
                    m_drawBuffer = new byte[outWidth * outHeight * 4];

                    m_LedState = getIBScanDevice().getOperableLEDs();

                    OnMsg_CaptureSeqStart();
                }
            } catch (IBScanException ibse) {
                m_bInitializing = false;

                if (ibse.getType().equals(IBScanException.Type.DEVICE_ACTIVE)) {
                    _SetStatusBarMessage("[Error Codigo =-203] La inicialización del dispositivo falló porque lo usa otro hilo / proceso.");
                } else if (ibse.getType().equals(IBScanException.Type.USB20_REQUIRED)) {
                    _SetStatusBarMessage("[Error Codigo =-209] La inicialización del dispositivo falló porque SDK solo funciona con USB 2.0.");
                } else {
                    _SetStatusBarMessage("La inicialización del dispositivo falló.");
                }

                OnMsg_UpdateDisplayResources();
            }
        }
    }

    protected Bitmap _CreateBitmap(int width, int height) {
        final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        if (bitmap != null) {
            final byte[] imageBuffer = new byte[width * height * 4];
            /*
             La imagen en el búfer se voltea verticalmente a partir
             de lo que espera la clase Bitmap;
             lo invertiremos para compensarlo mientras lo movemos al buffer.
             */
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    imageBuffer[(y * width + x) * 4] =
                            imageBuffer[(y * width + x) * 4 + 1] =
                                    imageBuffer[(y * width + x) * 4 + 2] =
                                            (byte) 128;
                    imageBuffer[(y * width + x) * 4 + 3] = (byte) 255;
                }
            }
            bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(imageBuffer));
        }
        return (bitmap);
    }

    protected void _CalculateScaleFactors(IBScanDevice.ImageData image, int outWidth, int outHeight) {
        int left = 0, top = 0;
        int tmp_width = outWidth;
        int tmp_height = outHeight;
        int imgWidth = image.width;
        int imgHeight = image.height;
        int dispWidth, dispHeight, dispImgX, dispImgY;

        if (outWidth > imgWidth) {
            tmp_width = imgWidth;
            left = (outWidth - imgWidth) / 2;
        }
        if (outHeight > imgHeight) {
            tmp_height = imgHeight;
            top = (outHeight - imgHeight) / 2;
        }

        float ratio_width = (float) tmp_width / (float) imgWidth;
        float ratio_height = (float) tmp_height / (float) imgHeight;

        dispWidth = outWidth;
        dispHeight = outHeight;

        if (ratio_width >= ratio_height) {
            dispWidth = tmp_height * imgWidth / imgHeight;
            dispWidth -= (dispWidth % 4);
            dispHeight = tmp_height;
            dispImgX = (tmp_width - dispWidth) / 2 + left;
            dispImgY = top;
        } else {
            dispWidth = tmp_width;
            dispWidth -= (dispWidth % 4);
            dispHeight = tmp_width * imgHeight / imgWidth;
            dispImgX = left;
            dispImgY = (tmp_height - dispHeight) / 2 + top;
        }

        if (dispImgX < 0) {
            dispImgX = 0;
        }
        if (dispImgY < 0) {
            dispImgY = 0;
        }

        ///////////////////////////////////////////////////////////////////////////////////
        m_scaleFactor = (double) dispWidth / image.width;
        m_leftMargin = dispImgX;
        m_topMargin = dispImgY;
        ///////////////////////////////////////////////////////////////////////////////////
    }

    protected void _DrawOverlay_ImageText(Canvas canvas) {
/*
 * Dibujar texto sobre imagen de mapa de bits
 		Paint g = new Paint();
		g.setAntiAlias(true);
		if (m_bNeedClearPlaten)
			g.setColor(Color.RED);
		else
			g.setColor(Color.BLUE);
		g.setTypeface(Typeface.DEFAULT);
		g.setTextSize(20);
//		canvas.drawText(m_strImageMessage, 10, 20, g);
		canvas.drawText(m_strImageMessage, 20, 40, g);
*/
    }

    protected void _DrawOverlay_WarningOfClearPlaten(Canvas canvas, int left, int top, int width, int height) {
        if (getIBScanDevice() == null)
            return;

        boolean idle = !m_bInitializing && (m_nCurrentCaptureStep == -1);

        if (!idle && m_bNeedClearPlaten && m_bBlank) {
            Paint g = new Paint();
            g.setStyle(Paint.Style.STROKE);
            g.setColor(Color.RED);
//			g.setStrokeWidth(10);
            g.setStrokeWidth(20);
            g.setAntiAlias(true);
            canvas.drawRect(left, top, width - 1, height - 1, g);
        }
    }

    protected void _DrawOverlay_ResultSegmentImage(Canvas canvas, IBScanDevice.ImageData image, int outWidth, int outHeight) {
        if (image.isFinal) {
//			if (m_chkDrawSegmentImage.isSelected())
            {
                // Dibujar un cuadrángulo para la imagen del segmento

                _CalculateScaleFactors(image, outWidth, outHeight);
                Paint g = new Paint();
                g.setColor(Color.rgb(0, 128, 0));
//				g.setStrokeWidth(1);
                g.setStrokeWidth(4);
                g.setAntiAlias(true);
                for (int i = 0; i < m_nSegmentImageArrayCount; i++) {
                    int x1, x2, x3, x4, y1, y2, y3, y4;
                    x1 = m_leftMargin + (int) (m_SegmentPositionArray[i].x1 * m_scaleFactor);
                    x2 = m_leftMargin + (int) (m_SegmentPositionArray[i].x2 * m_scaleFactor);
                    x3 = m_leftMargin + (int) (m_SegmentPositionArray[i].x3 * m_scaleFactor);
                    x4 = m_leftMargin + (int) (m_SegmentPositionArray[i].x4 * m_scaleFactor);
                    y1 = m_topMargin + (int) (m_SegmentPositionArray[i].y1 * m_scaleFactor);
                    y2 = m_topMargin + (int) (m_SegmentPositionArray[i].y2 * m_scaleFactor);
                    y3 = m_topMargin + (int) (m_SegmentPositionArray[i].y3 * m_scaleFactor);
                    y4 = m_topMargin + (int) (m_SegmentPositionArray[i].y4 * m_scaleFactor);

                    canvas.drawLine(x1, y1, x2, y2, g);
                    canvas.drawLine(x2, y2, x3, y3, g);
                    canvas.drawLine(x3, y3, x4, y4, g);
                    canvas.drawLine(x4, y4, x1, y1, g);
                }
            }
        }
    }

    protected void _DrawOverlay_RollGuideLine(Canvas canvas, IBScanDevice.ImageData image, int width, int height) {
        if (getIBScanDevice() == null || m_nCurrentCaptureStep == -1)
            return;

        if (m_ImageType == IBScanDevice.ImageType.ROLL_SINGLE_FINGER) {
            Paint g = new Paint();
            IBScanDevice.RollingData rollingdata;
            g.setAntiAlias(true);
            try {
                rollingdata = getIBScanDevice().getRollingInfo();

            } catch (IBScanException e) {
                rollingdata = null;
            }

            if ((rollingdata != null) && rollingdata.rollingLineX > 0 &&
                    (rollingdata.rollingState.equals(IBScanDevice.RollingState.TAKE_ACQUISITION) ||
                            rollingdata.rollingState.equals(IBScanDevice.RollingState.COMPLETE_ACQUISITION))) {
                _CalculateScaleFactors(image, width, height);
                int LineX = m_leftMargin + (int) (rollingdata.rollingLineX * m_scaleFactor);

                // Guide line for rolling
                if (rollingdata.rollingState.equals(IBScanDevice.RollingState.TAKE_ACQUISITION))
                    g.setColor(Color.RED);
                else if (rollingdata.rollingState.equals(IBScanDevice.RollingState.COMPLETE_ACQUISITION))
                    g.setColor(Color.GREEN);

                if (rollingdata.rollingLineX > -1) {
//					g.setStrokeWidth(2);
                    g.setStrokeWidth(4);
                    canvas.drawLine(LineX, 0, LineX, height, g);
                }
            }
        }
    }


    protected void _BeepFail() {
        try {
            IBScanDevice.BeeperType beeperType = getIBScanDevice().getOperableBeeper();
            if (beeperType != IBScanDevice.BeeperType.BEEPER_TYPE_NONE) {
                getIBScanDevice().setBeeper(IBScanDevice.BeepPattern.BEEP_PATTERN_GENERIC, 2/*Sol*/, 12/*300ms = 12*25ms*/, 0, 0);
                _Sleep(150);
                getIBScanDevice().setBeeper(IBScanDevice.BeepPattern.BEEP_PATTERN_GENERIC, 2/*Sol*/, 6/*150ms = 6*25ms*/, 0, 0);
                _Sleep(150);
                getIBScanDevice().setBeeper(IBScanDevice.BeepPattern.BEEP_PATTERN_GENERIC, 2/*Sol*/, 6/*150ms = 6*25ms*/, 0, 0);
                _Sleep(150);
                getIBScanDevice().setBeeper(IBScanDevice.BeepPattern.BEEP_PATTERN_GENERIC, 2/*Sol*/, 6/*150ms = 6*25ms*/, 0, 0);
            }
        } catch (IBScanException ibse) {
            // dispositivos para sin chip bip
            // envía el tono a la secuencia de "alarma" (los bips clásicos van allí)
            // con un volumen del 30%
            ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 30);
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 300); // 300 is duration in ms
            _Sleep(300 + 150);
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 150); // 150 is duration in ms
            _Sleep(150 + 150);
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 150); // 150 is duration in ms
            _Sleep(150 + 150);
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 150); // 150 is duration in ms
        }
    }

    protected void _BeepSuccess() {
        try {
            IBScanDevice.BeeperType beeperType = getIBScanDevice().getOperableBeeper();
            if (beeperType != IBScanDevice.BeeperType.BEEPER_TYPE_NONE) {
                getIBScanDevice().setBeeper(IBScanDevice.BeepPattern.BEEP_PATTERN_GENERIC, 2/*Sol*/, 4/*100ms = 4*25ms*/, 0, 0);
                _Sleep(50);
                getIBScanDevice().setBeeper(IBScanDevice.BeepPattern.BEEP_PATTERN_GENERIC, 2/*Sol*/, 4/*100ms = 4*25ms*/, 0, 0);
            }
        } catch (IBScanException ibse) {
            // dispositivos para sin chip bip
            // envía el tono a la secuencia de "alarma" (los bips clásicos van allí)
            // con un volumen del 30%
            ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 30);
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 100); // 100 is duration in ms
            _Sleep(100 + 50);
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 100); // 100 is duration in ms
        }
    }

    protected void _BeepOk() {
        try {
            IBScanDevice.BeeperType beeperType = getIBScanDevice().getOperableBeeper();
            if (beeperType != IBScanDevice.BeeperType.BEEPER_TYPE_NONE) {
                getIBScanDevice().setBeeper(IBScanDevice.BeepPattern.BEEP_PATTERN_GENERIC, 2/*Sol*/, 4/*100ms = 4*25ms*/, 0, 0);
            }
        } catch (IBScanException ibse) {
            // dispositivos para sin chip bip
            // envía el tono a la secuencia de "alarma" (los bips clásicos van allí)
            // con un volumen del 30%
            ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 30);
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 100); // 100 is duration in ms
        }
    }

    protected void _BeepDeviceCommunicationBreak() {
        for (int i = 0; i < 8; i++) {
            // envía el tono a la secuencia de "alarma" (los bips clásicos van allí)
            // con un volumen del 30%
            ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 30);
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 100); // 100 is duration in ms
            _Sleep(100 + 100);
        }
    }

    protected void _Sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
        }
    }


    protected void _SetImageMessage(String s) {
        m_strImageMessage = s;
    }


    protected void _AddCaptureSeqVector(String PreCaptureMessage, String PostCaptuerMessage,
                                        IBScanDevice.ImageType imageType, int NumberOfFinger, String fingerName) {
        SecondFragment.CaptureInfo info = new SecondFragment.CaptureInfo();
        info.PreCaptureMessage = PreCaptureMessage;
        info.PostCaptuerMessage = PostCaptuerMessage;
        info.ImageType = imageType;
        info.NumberOfFinger = NumberOfFinger;
        info.fingerName = fingerName;
        m_vecCaptureSeq.addElement(info);
    }

    protected void _UpdateCaptureSequences() {
        try {
            // almacenar el dispositivo actualmente seleccionado
            String strSelectedText = "";
            strSelectedText = CAPTURE_SEQ_FLAT_SINGLE_FINGER;

            // poblar el combobox
            m_arrCaptureSeq = new ArrayList<String>();

            m_arrCaptureSeq.add("- Por favor selecciona -");
            final int devIndex = this.m_cboUsbDevices.getSelectedItemPosition() - 1;
            if (devIndex > -1) {
                IBScan.DeviceDesc devDesc = getIBScan().getDeviceDescription(devIndex);
                if ((devDesc.productName.equals("WATSON")) ||
                        (devDesc.productName.equals("WATSON MINI")) ||
                        (devDesc.productName.equals("SHERLOCK_ROIC")) ||
                        (devDesc.productName.equals("SHERLOCK"))) {
                    m_arrCaptureSeq.add(CAPTURE_SEQ_FLAT_SINGLE_FINGER);
                    m_arrCaptureSeq.add(CAPTURE_SEQ_ROLL_SINGLE_FINGER);
                    m_arrCaptureSeq.add(CAPTURE_SEQ_2_FLAT_FINGERS);
                    m_arrCaptureSeq.add(CAPTURE_SEQ_10_SINGLE_FLAT_FINGERS);
                    m_arrCaptureSeq.add(CAPTURE_SEQ_10_SINGLE_ROLLED_FINGERS);
                } else if ((devDesc.productName.equals("COLUMBO")) ||
                        (devDesc.productName.equals("CURVE"))) {
                    m_arrCaptureSeq.add(CAPTURE_SEQ_FLAT_SINGLE_FINGER);
                    m_arrCaptureSeq.add(CAPTURE_SEQ_10_SINGLE_FLAT_FINGERS);
                } else if ((devDesc.productName.equals("HOLMES")) ||
                        (devDesc.productName.equals("KOJAK")) ||
                        (devDesc.productName.equals("FIVE-0"))) {
                    m_arrCaptureSeq.add(CAPTURE_SEQ_FLAT_SINGLE_FINGER);
                    m_arrCaptureSeq.add(CAPTURE_SEQ_ROLL_SINGLE_FINGER);
                    m_arrCaptureSeq.add(CAPTURE_SEQ_2_FLAT_FINGERS);
                    m_arrCaptureSeq.add(CAPTURE_SEQ_4_FLAT_FINGERS);
                    m_arrCaptureSeq.add(CAPTURE_SEQ_10_SINGLE_FLAT_FINGERS);
                    m_arrCaptureSeq.add(CAPTURE_SEQ_10_SINGLE_ROLLED_FINGERS);
                    m_arrCaptureSeq.add(CAPTURE_SEQ_10_FLAT_WITH_4_FINGER_SCANNER);
                }
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                    R.layout.spinner_text_layout, m_arrCaptureSeq);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

//			if (selectedSeq > -1)
//				this.m_cboCaptureSeq.setse(strSelectedText);

            OnMsg_UpdateDisplayResources();
        } catch (IBScanException e) {
            e.printStackTrace();
        }
    }

    protected void _ReleaseDevice() throws IBScanException {
        if (getIBScanDevice() != null) {
            if (getIBScanDevice().isOpened() == true) {
                getIBScanDevice().close();
                setIBScanDevice(null);
            }
        }

        m_nCurrentCaptureStep = -1;
        m_bInitializing = false;
    }

    public void _SetLEDs(SecondFragment.CaptureInfo info, int ledColor, boolean bBlink) {
        try {
            IBScanDevice.LedState ledState = getIBScanDevice().getOperableLEDs();
            if (ledState.ledCount == 0) {
                return;
            }
        } catch (IBScanException ibse) {
            ibse.printStackTrace();
        }

        int setLEDs = 0;

        if (ledColor == __LED_COLOR_NONE__) {
            try {
                getIBScanDevice().setLEDs(setLEDs);
            } catch (IBScanException ibse) {
                ibse.printStackTrace();
            }

            return;
        }

        if (m_LedState.ledType == IBScanDevice.LedType.FSCAN) {
            if (bBlink) {
                if (ledColor == __LED_COLOR_GREEN__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_BLINK_GREEN;
                } else if (ledColor == __LED_COLOR_RED__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_BLINK_RED;
                } else if (ledColor == __LED_COLOR_YELLOW__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_BLINK_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_BLINK_RED;
                }
            }

            if (info.ImageType == IBScanDevice.ImageType.ROLL_SINGLE_FINGER) {
                setLEDs |= IBScanDevice.IBSU_LED_F_PROGRESS_ROLL;
            }

            if ((info.fingerName.equals("SFF_Right_Thumb")) ||
                    (info.fingerName.equals("SRF_Right_Thumb"))) {
                setLEDs |= IBScanDevice.IBSU_LED_F_PROGRESS_TWO_THUMB;
                if (ledColor == __LED_COLOR_GREEN__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_THUMB_GREEN;
                } else if (ledColor == __LED_COLOR_RED__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_THUMB_RED;
                } else if (ledColor == __LED_COLOR_YELLOW__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_THUMB_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_THUMB_RED;
                }
            } else if ((info.fingerName.equals("SFF_Left_Thumb")) ||
                    (info.fingerName.equals("SRF_Left_Thumb"))) {
                setLEDs |= IBScanDevice.IBSU_LED_F_PROGRESS_TWO_THUMB;
                if (ledColor == __LED_COLOR_GREEN__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_THUMB_GREEN;
                } else if (ledColor == __LED_COLOR_RED__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_THUMB_RED;
                } else if (ledColor == __LED_COLOR_YELLOW__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_THUMB_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_THUMB_RED;
                }
            } else if ((info.fingerName.equals("TFF_2_Thumbs"))) {
                setLEDs |= IBScanDevice.IBSU_LED_F_PROGRESS_TWO_THUMB;
                if (ledColor == __LED_COLOR_GREEN__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_THUMB_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_THUMB_GREEN;
                } else if (ledColor == __LED_COLOR_RED__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_THUMB_RED;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_THUMB_RED;
                } else if (ledColor == __LED_COLOR_YELLOW__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_THUMB_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_THUMB_RED;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_THUMB_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_THUMB_RED;
                }
            }
            ///////////////////MANO IZQUIERDA////////////////////
            else if ((info.fingerName.equals("SFF_Left_Index")) ||
                    (info.fingerName.equals("SRF_Left_Index"))) {
                setLEDs |= IBScanDevice.IBSU_LED_F_PROGRESS_LEFT_HAND;
                if (ledColor == __LED_COLOR_GREEN__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_INDEX_GREEN;
                } else if (ledColor == __LED_COLOR_RED__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_INDEX_RED;
                } else if (ledColor == __LED_COLOR_YELLOW__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_INDEX_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_INDEX_RED;
                }
            } else if ((info.fingerName.equals("SFF_Left_Middle")) ||
                    (info.fingerName.equals("SRF_Left_Middle"))) {
                setLEDs |= IBScanDevice.IBSU_LED_F_PROGRESS_LEFT_HAND;
                if (ledColor == __LED_COLOR_GREEN__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_MIDDLE_GREEN;
                } else if (ledColor == __LED_COLOR_RED__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_MIDDLE_RED;
                } else if (ledColor == __LED_COLOR_YELLOW__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_MIDDLE_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_MIDDLE_RED;
                }
            } else if ((info.fingerName.equals("SFF_Left_Ring")) ||
                    (info.fingerName.equals("SRF_Left_Ring"))) {
                setLEDs |= IBScanDevice.IBSU_LED_F_PROGRESS_LEFT_HAND;
                if (ledColor == __LED_COLOR_GREEN__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_RING_GREEN;
                } else if (ledColor == __LED_COLOR_RED__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_RING_RED;
                } else if (ledColor == __LED_COLOR_YELLOW__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_RING_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_RING_RED;
                }
            } else if ((info.fingerName.equals("SFF_Left_Little")) ||
                    (info.fingerName.equals("SRF_Left_Little"))) {
                setLEDs |= IBScanDevice.IBSU_LED_F_PROGRESS_LEFT_HAND;
                if (ledColor == __LED_COLOR_GREEN__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_LITTLE_GREEN;
                } else if (ledColor == __LED_COLOR_RED__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_LITTLE_RED;
                } else if (ledColor == __LED_COLOR_YELLOW__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_LITTLE_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_LITTLE_RED;
                }
            } else if ((info.fingerName.equals("4FF_Left_4_Fingers"))) {
                setLEDs |= IBScanDevice.IBSU_LED_F_PROGRESS_LEFT_HAND;
                if (ledColor == __LED_COLOR_GREEN__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_INDEX_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_MIDDLE_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_RING_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_LITTLE_GREEN;
                } else if (ledColor == __LED_COLOR_RED__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_INDEX_RED;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_MIDDLE_RED;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_RING_RED;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_LITTLE_RED;
                } else if (ledColor == __LED_COLOR_YELLOW__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_INDEX_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_MIDDLE_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_RING_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_LITTLE_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_INDEX_RED;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_MIDDLE_RED;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_RING_RED;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_LITTLE_RED;
                }
            }
            ///////////MANO DERECHA /////////////////////////
            else if ((info.fingerName.equals("SFF_Right_Index")) ||
                    (info.fingerName.equals("SRF_Right_Index"))) {
                setLEDs |= IBScanDevice.IBSU_LED_F_PROGRESS_RIGHT_HAND;
                if (ledColor == __LED_COLOR_GREEN__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_INDEX_GREEN;
                } else if (ledColor == __LED_COLOR_RED__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_INDEX_RED;
                } else if (ledColor == __LED_COLOR_YELLOW__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_INDEX_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_INDEX_RED;
                }
            } else if ((info.fingerName.equals("SFF_Right_Middle")) ||
                    (info.fingerName.equals("SRF_Right_Middle"))) {
                setLEDs |= IBScanDevice.IBSU_LED_F_PROGRESS_RIGHT_HAND;
                if (ledColor == __LED_COLOR_GREEN__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_MIDDLE_GREEN;
                } else if (ledColor == __LED_COLOR_RED__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_MIDDLE_RED;
                } else if (ledColor == __LED_COLOR_YELLOW__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_MIDDLE_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_MIDDLE_RED;
                }
            } else if ((info.fingerName.equals("SFF_Right_Ring")) ||
                    (info.fingerName.equals("SRF_Right_Ring"))) {
                setLEDs |= IBScanDevice.IBSU_LED_F_PROGRESS_RIGHT_HAND;
                if (ledColor == __LED_COLOR_GREEN__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_RING_GREEN;
                } else if (ledColor == __LED_COLOR_RED__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_RING_RED;
                } else if (ledColor == __LED_COLOR_YELLOW__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_RING_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_RING_RED;
                }
            } else if ((info.fingerName.equals("SFF_Right_Little")) ||
                    (info.fingerName.equals("SRF_Right_Little"))) {
                setLEDs |= IBScanDevice.IBSU_LED_F_PROGRESS_RIGHT_HAND;
                if (ledColor == __LED_COLOR_GREEN__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_LITTLE_GREEN;
                } else if (ledColor == __LED_COLOR_RED__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_LITTLE_RED;
                } else if (ledColor == __LED_COLOR_YELLOW__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_LITTLE_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_LITTLE_RED;
                }
            } else if ((info.fingerName.equals("4FF_Right_4_Fingers"))) {
                setLEDs |= IBScanDevice.IBSU_LED_F_PROGRESS_RIGHT_HAND;
                if (ledColor == __LED_COLOR_GREEN__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_INDEX_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_MIDDLE_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_RING_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_LITTLE_GREEN;
                } else if (ledColor == __LED_COLOR_RED__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_INDEX_RED;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_MIDDLE_RED;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_RING_RED;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_LITTLE_RED;
                } else if (ledColor == __LED_COLOR_YELLOW__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_INDEX_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_MIDDLE_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_RING_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_LITTLE_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_INDEX_RED;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_MIDDLE_RED;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_RING_RED;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_LITTLE_RED;
                }
            }

            if (ledColor == __LED_COLOR_NONE__) {
                setLEDs = 0;
            }

            try {
                getIBScanDevice().setLEDs(setLEDs);
            } catch (IBScanException ibse) {
                ibse.printStackTrace();
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    // Event-dispatch threads
    private void OnMsg_SetStatusBarMessage(final String s) {
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                _SetStatusBarMessage(s);
            }
        });
    }


    private void OnMsg_Beep(final int beepType) {
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (beepType == __BEEP_FAIL__)
                    _BeepFail();
                else if (beepType == __BEEP_SUCCESS__)
                    _BeepSuccess();
                else if (beepType == __BEEP_OK__)
                    _BeepOk();
                else if (beepType == __BEEP_DEVICE_COMMUNICATION_BREAK__)
                    _BeepDeviceCommunicationBreak();
            }
        });
    }

    private void OnMsg_CaptureSeqStart() {
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (getIBScanDevice() == null) {
                    OnMsg_UpdateDisplayResources();
                    return;
                }

                String strCaptureSeq = CAPTURE_SEQ_FLAT_SINGLE_FINGER;

                m_vecCaptureSeq.clear();

/** Por favor, consulte la definición a continuación
 protected final String CAPTURE_SEQ_FLAT_SINGLE_FINGER 				= "Single flat finger";
 protected final String CAPTURE_SEQ_ROLL_SINGLE_FINGER 				= "Single rolled finger";
 protected final String CAPTURE_SEQ_2_FLAT_FINGERS 					= "2 flat fingers";
 protected final String CAPTURE_SEQ_10_SINGLE_FLAT_FINGERS 			= "10 single flat fingers";
 protected final String CAPTURE_SEQ_10_SINGLE_ROLLED_FINGERS 		= "10 single rolled fingers";
 protected final String CAPTURE_SEQ_4_FLAT_FINGERS 					= "4 flat fingers";
 protected final String CAPTURE_SEQ_10_FLAT_WITH_4_FINGER_SCANNER 	= "10 flat fingers with 4-finger scanner";
 */
                if (strCaptureSeq.equals(CAPTURE_SEQ_FLAT_SINGLE_FINGER)) {
                    _AddCaptureSeqVector("ponga un dedo!",
                            "Mantenga el dedo en el sensor!",
                            IBScanDevice.ImageType.FLAT_SINGLE_FINGER,
                            1,
                            "SFF_Unknown");
                }


                if (strCaptureSeq.equals(CAPTURE_SEQ_ROLL_SINGLE_FINGER)) {
                    _AddCaptureSeqVector("Por favor, ponga un dedo en el sensor!",
                            "Rodar dedo!",
                            IBScanDevice.ImageType.ROLL_SINGLE_FINGER,
                            1,
                            "SRF_Unknown");
                }

                if (strCaptureSeq == CAPTURE_SEQ_2_FLAT_FINGERS) {
                    _AddCaptureSeqVector("Por favor, ponga dos dedos en el sensor!",
                            "Mantenga los dedos en el sensor!",
                            IBScanDevice.ImageType.FLAT_TWO_FINGERS,
                            2,
                            "TFF_Unknown");
                }

                if (strCaptureSeq == CAPTURE_SEQ_10_SINGLE_FLAT_FINGERS) {
                    _AddCaptureSeqVector("Por favor, ponga el pulgar derecho en el sensor!",
                            "Mantenga los dedos en el sensor!",
                            IBScanDevice.ImageType.FLAT_SINGLE_FINGER,
                            1,
                            "SFF_Right_Thumb");

                    _AddCaptureSeqVector("Por favor ponga el índice correcto en el sensor!",
                            "Mantenga los dedos en el sensor!",
                            IBScanDevice.ImageType.FLAT_SINGLE_FINGER,
                            1,
                            "SFF_Right_Index");

                    _AddCaptureSeqVector("Por favor ponga el centro correcto en el sensor!",
                            "Mantenga los dedos en el sensor!",
                            IBScanDevice.ImageType.FLAT_SINGLE_FINGER,
                            1,
                            "SFF_Right_Middle");

                    _AddCaptureSeqVector("Por favor ponga el anillo derecho en el sensor!",
                            "Mantenga los dedos en el sensor!",
                            IBScanDevice.ImageType.FLAT_SINGLE_FINGER,
                            1,
                            "SFF_Right_Ring");

                    _AddCaptureSeqVector("Por favor, ponga poco a la derecha en el sensor!",
                            "Mantenga los dedos en el sensor!",
                            IBScanDevice.ImageType.FLAT_SINGLE_FINGER,
                            1,
                            "SFF_Right_Little");

                    _AddCaptureSeqVector("Por favor ponga el pulgar izquierdo en el sensor!",
                            "Mantenga los dedos en el sensor!",
                            IBScanDevice.ImageType.FLAT_SINGLE_FINGER,
                            1,
                            "SFF_Left_Thumb");

                    _AddCaptureSeqVector("Por favor ponga el índice izquierdo en el sensor!",
                            "Mantenga los dedos en el sensor!",
                            IBScanDevice.ImageType.FLAT_SINGLE_FINGER,
                            1,
                            "SFF_Left_Index");

                    _AddCaptureSeqVector("Por favor ponga el medio izquierdo en el sensor!",
                            "Mantenga los dedos en el sensor!",
                            IBScanDevice.ImageType.FLAT_SINGLE_FINGER,
                            1,
                            "SFF_Left_Middle");

                    _AddCaptureSeqVector("Por favor ponga el anillo izquierdo en el sensor!",
                            "Mantenga los dedos en el sensor!",
                            IBScanDevice.ImageType.FLAT_SINGLE_FINGER,
                            1,
                            "SFF_Left_Ring");

                    _AddCaptureSeqVector("Por favor, ponga poco a la izquierda en el sensor!",
                            "Mantenga los dedos en el sensor!",
                            IBScanDevice.ImageType.FLAT_SINGLE_FINGER,
                            1,
                            "SFF_Left_Little");
                }

                if (strCaptureSeq == CAPTURE_SEQ_10_SINGLE_ROLLED_FINGERS) {
                    _AddCaptureSeqVector("Por favor, ponga el pulgar derecho en el sensor!",
                            "Rodar dedo!",
                            IBScanDevice.ImageType.ROLL_SINGLE_FINGER,
                            1,
                            "SFF_Right_Thumb");

                    _AddCaptureSeqVector("Por favor ponga el índice correcto en el sensor!",
                            "Rodar dedo!",
                            IBScanDevice.ImageType.ROLL_SINGLE_FINGER,
                            1,
                            "SFF_Right_Index");

                    _AddCaptureSeqVector("Por favor ponga el centro correcto en el sensor!",
                            "Rodar dedo!",
                            IBScanDevice.ImageType.ROLL_SINGLE_FINGER,
                            1,
                            "SFF_Right_Middle");

                    _AddCaptureSeqVector("Por favor ponga el anillo derecho en el sensor!",
                            "Rodar dedo!",
                            IBScanDevice.ImageType.ROLL_SINGLE_FINGER,
                            1,
                            "SFF_Right_Ring");

                    _AddCaptureSeqVector("Por favor, ponga poco a la derecha en el sensor!",
                            "Rodar dedo!",
                            IBScanDevice.ImageType.ROLL_SINGLE_FINGER,
                            1,
                            "SFF_Right_Little");

                    _AddCaptureSeqVector("Por favor ponga el pulgar izquierdo en el sensor!",
                            "Rodar dedo!",
                            IBScanDevice.ImageType.ROLL_SINGLE_FINGER,
                            1,
                            "SFF_Left_Thumb");

                    _AddCaptureSeqVector("Por favor ponga el índice izquierdo en el sensor!",
                            "Rodar dedo!",
                            IBScanDevice.ImageType.ROLL_SINGLE_FINGER,
                            1,
                            "SFF_Left_Index");

                    _AddCaptureSeqVector("Por favor ponga el medio izquierdo en el sensor!",
                            "Rodar dedo!",
                            IBScanDevice.ImageType.ROLL_SINGLE_FINGER,
                            1,
                            "SFF_Left_Middle");

                    _AddCaptureSeqVector("Por favor ponga el anillo izquierdo en el sensor!",
                            "Rodar dedo!",
                            IBScanDevice.ImageType.ROLL_SINGLE_FINGER,
                            1,
                            "SFF_Left_Ring");

                    _AddCaptureSeqVector("Por favor, ponga poco a la izquierda en el sensor!",
                            "Rodar dedo!",
                            IBScanDevice.ImageType.ROLL_SINGLE_FINGER,
                            1,
                            "SFF_Left_Little");
                }

                if (strCaptureSeq == CAPTURE_SEQ_4_FLAT_FINGERS) {
                    _AddCaptureSeqVector("Por favor, ponga 4 dedos en el sensor!",
                            "Mantenga los dedos en el sensor!",
                            IBScanDevice.ImageType.FLAT_FOUR_FINGERS,
                            4,
                            "4FF_Unknown");
                }

                if (strCaptureSeq == CAPTURE_SEQ_10_FLAT_WITH_4_FINGER_SCANNER) {
                    _AddCaptureSeqVector("Por favor, ponga a la derecha 4 dedos en el sensor!",
                            "Mantenga los dedos en el sensor!",
                            IBScanDevice.ImageType.FLAT_FOUR_FINGERS,
                            4,
                            "4FF_Right_4_Fingers");

                    _AddCaptureSeqVector("Por favor, coloque los 4 dedos izquierdos en el sensor!",
                            "Mantenga los dedos en el sensor!",
                            IBScanDevice.ImageType.FLAT_FOUR_FINGERS,
                            4,
                            "4FF_Left_4_Fingers");

                    _AddCaptureSeqVector("Por favor, ponga 2-thumbs en el sensor!",
                            "Mantenga los dedos en el sensor!",
                            IBScanDevice.ImageType.FLAT_TWO_FINGERS,
                            2,
                            "TFF_2_Thumbs");
                }

                OnMsg_CaptureSeqNext();
            }
        });
    }

    private void OnMsg_CaptureSeqNext() {
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (getIBScanDevice() == null)
                    return;

                m_bBlank = false;
                for (int i = 0; i < 4; i++)
                    m_FingerQuality[i] = IBScanDevice.FingerQualityState.FINGER_NOT_PRESENT;

                m_nCurrentCaptureStep++;
                if (m_nCurrentCaptureStep >= m_vecCaptureSeq.size()) {
                    // All of capture sequence completely
                    SecondFragment.CaptureInfo tmpInfo = new SecondFragment.CaptureInfo();
                    _SetLEDs(tmpInfo, __LED_COLOR_NONE__, false);
                    m_nCurrentCaptureStep = -1;

                    OnMsg_UpdateDisplayResources();
                    return;
                }

                try {
/*					if (m_chkDetectSmear.isSelected())
					{
						getIBScanDevice().setProperty(IBScanDevice.PropertyId.ROLL_MODE, "1");
						String strValue = String.valueOf(m_cboSmearLevel.getSelectedIndex());
						getIBScanDevice().setProperty(IBScanDevice.PropertyId.ROLL_LEVEL, strValue);
			}
			else
			{
						getIBScanDevice().setProperty(IBScanDevice.PropertyId.ROLL_MODE, "0");
					}
*/
                    // Realice un retraso de captura para mostrar la imagen
                    // del resultado en el modo de captura múltiple (500 ms)
                    if (m_nCurrentCaptureStep > 0) {
                        _Sleep(500);
                        m_strImageMessage = "";
                    }

                    SecondFragment.CaptureInfo info = m_vecCaptureSeq.elementAt(m_nCurrentCaptureStep);

                    IBScanDevice.ImageResolution imgRes = IBScanDevice.ImageResolution.RESOLUTION_500;
                    boolean bAvailable = getIBScanDevice().isCaptureAvailable(info.ImageType, imgRes);
                    if (!bAvailable) {
                        _SetStatusBarMessage("El modo de captura (" + info.ImageType + ") no está disponible");
                        m_nCurrentCaptureStep = -1;
                        OnMsg_UpdateDisplayResources();
                        return;
                    }

                    // Iniciar captura
                    int captureOptions = 0;
//					if (m_chkAutoContrast.isSelected())
                    captureOptions |= IBScanDevice.OPTION_AUTO_CONTRAST;
//					if (m_chkAutoCapture.isSelected())
                    captureOptions |= IBScanDevice.OPTION_AUTO_CAPTURE;
//					if (m_chkIgnoreFingerCount.isSelected())
                    captureOptions |= IBScanDevice.OPTION_IGNORE_FINGER_COUNT;

                    getIBScanDevice().beginCaptureImage(info.ImageType, imgRes, captureOptions);

                    String strMessage = info.PreCaptureMessage;
                    _SetStatusBarMessage(strMessage);
//					if (!m_chkAutoCapture.isSelected())
//						strMessage += "\r\nPress button 'Take Result Image' when image is good!";

                    _SetImageMessage(strMessage);
                    m_strImageMessage = strMessage;

                    m_ImageType = info.ImageType;

                    _SetLEDs(info, __LED_COLOR_RED__, true);

                    OnMsg_UpdateDisplayResources();
                } catch (IBScanException ibse) {
                    ibse.printStackTrace();
                    _SetStatusBarMessage("No se pudo ejecutar beginCaptureImage()");
                    m_nCurrentCaptureStep = -1;
                }
            }
        });
    }

    private void OnMsg_cboUsbDevice_Changed() {
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (m_nSelectedDevIndex == m_cboUsbDevices.getSelectedItemPosition())
                    return;

                m_nSelectedDevIndex = m_cboUsbDevices.getSelectedItemPosition();
                if (getIBScanDevice() != null) {
                    try {
                        _ReleaseDevice();
                    } catch (IBScanException ibse) {
                        ibse.printStackTrace();
                    }
                }

                _UpdateCaptureSequences();
            }
        });
    }

    private void OnMsg_UpdateDeviceList(final boolean bConfigurationChanged) {
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                try {
                    boolean idle = (!m_bInitializing && (m_nCurrentCaptureStep == -1)) ||
                            (bConfigurationChanged);

                    if (idle) {
                        m_btnCaptureStop.setEnabled(false);
                        m_btnCaptureStart.setEnabled(false);
                    }

                    //almacenar el dispositivo actualmente seleccionado
                    String strSelectedText = "";
                    int selectedDev = m_cboUsbDevices.getSelectedItemPosition();
                    if (selectedDev > -1)
                        strSelectedText = m_cboUsbDevices.getSelectedItem().toString();

                    m_arrUsbDevices = new ArrayList<String>();

                    m_arrUsbDevices.add("- Por favor selecciona -");
                    // poblar combo box
                    int devices = getIBScan().getDeviceCount();
//					m_cboUsbDevices.setMaximumRowCount(devices + 1);

                    selectedDev = 0;
                    for (int i = 0; i < devices; i++) {
                        IBScan.DeviceDesc devDesc = getIBScan().getDeviceDescription(i);
                        String strDevice;
                        strDevice = devDesc.productName + "_v" + devDesc.fwVersion + "(" + devDesc.serialNumber + ")";

                        m_arrUsbDevices.add(strDevice);
                        if (strDevice == strSelectedText)
                            selectedDev = i + 1;
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                            R.layout.spinner_text_layout, m_arrUsbDevices);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    m_cboUsbDevices.setAdapter(adapter);
                    m_cboUsbDevices.setOnItemSelectedListener(m_cboUsbDevicesItemSelectedListener);

                    if ((selectedDev == 0 && (m_cboUsbDevices.getCount() == 2)))
                        selectedDev = 1;

                    m_cboUsbDevices.setSelection(selectedDev);

                    if (idle) {
                        OnMsg_cboUsbDevice_Changed();
                        _UpdateCaptureSequences();
                    }
                } catch (IBScanException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void OnMsg_UpdateDisplayResources() {
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                boolean selectedDev = m_cboUsbDevices.getSelectedItemPosition() > 0;
                boolean idle = !m_bInitializing && (m_nCurrentCaptureStep == -1);
                boolean active = !m_bInitializing && (m_nCurrentCaptureStep != -1);
                boolean uninitializedDev = selectedDev && (getIBScanDevice() == null);

                m_cboUsbDevices.setEnabled(idle);

                m_btnCaptureStart.setEnabled(selectedDev);
                m_btnCaptureStop.setEnabled(active);

//				m_chkAutoContrast.setEnabled(selectedDev && idle );
//				m_chkAutoCapture.setEnabled(selectedDev && idle );
//				m_chkIgnoreFingerCount.setEnabled(selectedDev && idle );
//				m_chkSaveImages.setEnabled(selectedDev && idle );
//				m_btnImageFolder.setEnabled(selectedDev && idle );

//				m_chkUseClearPlaten.setEnabled(uninitializedDev);

                if (active) {
                    m_btnCaptureStart.setText("Tomar");
                } else {
                    m_btnCaptureStart.setText("Iniciar");
                }
            }
        });
    }

    private void OnMsg_AskRecapture(final IBScanException imageStatus) {
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                String askMsg;

                askMsg = "[Advertencia = " + imageStatus.getType().toString() + "] Quieres una recaptura?";

                AlertDialog.Builder dlgAskRecapture = new AlertDialog.Builder(getActivity().getApplicationContext());
                dlgAskRecapture.setMessage(askMsg);
                dlgAskRecapture.setPositiveButton("Si",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Para recuperar la posición actual de los dedos
                                m_nCurrentCaptureStep--;
                                OnMsg_CaptureSeqNext();
                            }
                        });
                dlgAskRecapture.setNegativeButton("No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                OnMsg_CaptureSeqNext();
                            }
                        });

                dlgAskRecapture.show();
            }
        });
    }


    private void OnMsg_DeviceCommunicationBreak() {
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (getIBScanDevice() == null)
                    return;

                _SetStatusBarMessage("La comunicación del dispositivo se rompió");

                try {
                    _ReleaseDevice();

                    OnMsg_Beep(__BEEP_DEVICE_COMMUNICATION_BREAK__);
                    OnMsg_UpdateDeviceList(false);
                } catch (IBScanException ibse) {
                    if (ibse.getType().equals(IBScanException.Type.RESOURCE_LOCKED)) {
                        OnMsg_DeviceCommunicationBreak();
                    }
                }
            }
        });
    }

    private void OnMsg_DrawImage(final IBScanDevice device, final IBScanDevice.ImageData image) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int destWidth = m_imgPreview.getWidth() - 20;
                int destHeight = m_imgPreview.getHeight() - 20;
//				int outImageSize = destWidth * destHeight;

                try {
                    if (destHeight <= 0 || destWidth <= 0)
                        return;

                    if (destWidth != m_BitmapImage.getWidth() || destHeight != m_BitmapImage.getHeight()) {
                        /*
                         si se cambia el tamaño de la imagen (p. ej., tipo de captura modificada
                         desde el dedo plano hasta el dedo enrollado) Crear bitmap nuevamente
                         */

                        m_BitmapImage = Bitmap.createBitmap(destWidth, destHeight, Bitmap.Config.ARGB_8888);
                        m_drawBuffer = new byte[destWidth * destHeight * 4];
                    }

                    if (image.isFinal) {
                        getIBScanDevice().generateDisplayImage(image.buffer, image.width, image.height,
                                m_drawBuffer, destWidth, destHeight, (byte) 255, 2 /*IBSU_IMG_FORMAT_RGB32*/, 2 /*HIGH QUALITY*/, true);
/*						for (int i=0; i<destWidth*destHeight; i++)
					{
							if (m_drawBuffer[i] != -1) {
								OnMsg_Beep(__BEEP_OK__);
						break;
					}
						}
*/
                    } else {
                        getIBScanDevice().generateDisplayImage(image.buffer, image.width, image.height,
                                m_drawBuffer, destWidth, destHeight, (byte) 255, 2 /*IBSU_IMG_FORMAT_RGB32*/, 0 /*LOW QUALITY*/, true);
                    }
                } catch (IBScanException e) {
                    e.printStackTrace();
                }

                m_BitmapImage.copyPixelsFromBuffer(ByteBuffer.wrap(m_drawBuffer));
                // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                Canvas canvas = new Canvas(m_BitmapImage);

                _DrawOverlay_ImageText(canvas);
                _DrawOverlay_WarningOfClearPlaten(canvas, 0, 0, destWidth, destHeight);
                _DrawOverlay_ResultSegmentImage(canvas, image, destWidth, destHeight);
                _DrawOverlay_RollGuideLine(canvas, image, destWidth, destHeight);
/*				_DrawOverlay_WarningOfClearPlaten(canvas, 0, 0, image.width, image.height);
				_DrawOverlay_ResultSegmentImage(canvas, image, image.width, image.height);
				_DrawOverlay_RollGuideLine(canvas, image, image.width, image.height);
			 */
                m_savedData.imageBitmap = m_BitmapImage;
                m_imgPreview.setImageBitmap(m_BitmapImage);
            }
        });
    }

    private void OnMsg_DrawFingerQuality() {
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                // Actualizar el valor en el campo fingerQuality y el botón flash.
                for (int i = 0; i < 4; i++) {
                    int color;
                    if (m_FingerQuality[i] == IBScanDevice.FingerQualityState.GOOD)
                        color = Color.rgb(0, 128, 0);
                    else if (m_FingerQuality[i] == IBScanDevice.FingerQualityState.FAIR)
                        color = Color.rgb(255, 128, 0);
                    else if (m_FingerQuality[i] == IBScanDevice.FingerQualityState.POOR ||
                            m_FingerQuality[i] == IBScanDevice.FingerQualityState.INVALID_AREA_TOP ||
                            m_FingerQuality[i] == IBScanDevice.FingerQualityState.INVALID_AREA_BOTTOM ||
                            m_FingerQuality[i] == IBScanDevice.FingerQualityState.INVALID_AREA_LEFT ||
                            m_FingerQuality[i] == IBScanDevice.FingerQualityState.INVALID_AREA_RIGHT
                            )
                        color = Color.rgb(255, 0, 0);
                    else
                        color = Color.LTGRAY;

                }
            }
        });
    }

    /*
     * Mostrar Toast mensaje en la UI thread.
     */
    private void showToastOnUiThread(final String message, final int duration) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(getActivity().getApplicationContext(), message, duration);
                toast.show();
            }
        });
    }
    
    /*
     * Adjunte el archivo a un correo electrónico y envíelo.
     */
    private void attachAndSendEmail(final ArrayList ur, final String subject, final String message) {
        final Intent i = new Intent(Intent.ACTION_SEND_MULTIPLE);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{""});
        i.putExtra(Intent.EXTRA_SUBJECT, subject);
//		i.putExtra(Intent.EXTRA_STREAM,  ur);
        i.putParcelableArrayListExtra(Intent.EXTRA_STREAM, ur);
        i.putExtra(Intent.EXTRA_TEXT, message);

        try {
            startActivity(Intent.createChooser(i, "Enviando email..."));
        } catch (ActivityNotFoundException anfe) {
            showToastOnUiThread("No hay clientes de correo electrónico instalados", Toast.LENGTH_LONG);
        }
    }
    

    /*
     * Exit application.
     */
    private static void exitApp(Activity ac) {
        ac.moveTaskToBack(true);
        ac.finish();
        android.os.Process.killProcess(android.os.Process.myPid());
    }



    /* *********************************************************************************************
     * EVENTOS HANDLERS
     ******************************************************************************************** */

    /*
     * Handle haga clic en el botón "Start capture".
     */
    private View.OnClickListener m_btnCaptureStartClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            if (m_bInitializing)
                return;

            int devIndex = m_cboUsbDevices.getSelectedItemPosition() - 1;
            if (devIndex < 0)
                return;

            if (m_nCurrentCaptureStep != -1) {
                try {
                    boolean IsActive = getIBScanDevice().isCaptureActive();
                    if (IsActive) {
                        // Capture la imagen manualmente para el dispositivo activo
                        getIBScanDevice().captureImageManually();
                        return;
                    }
                } catch (IBScanException ibse) {
                    _SetStatusBarMessage("IBScanDevice.takeResultImageManually() returned exception "
                            + ibse.getType().toString() + ".");
                }
            }

            if (getIBScanDevice() == null) {
                m_bInitializing = true;
                SecondFragment._InitializeDeviceThreadCallback thread = new SecondFragment._InitializeDeviceThreadCallback(m_nSelectedDevIndex - 1);
                thread.start();
            } else {
                OnMsg_CaptureSeqStart();
            }

            OnMsg_UpdateDisplayResources();
        }
    };

    /*
     * Handle haga clic en el botón "Stop capture".
     */
    private View.OnClickListener m_btnCaptureStopClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            if (getIBScanDevice() == null)
                return;

            // Cancele la captura de la imagen para el dispositivo activo
            try {
                // Cancele la captura de la imagen para el dispositivo activo.
                getIBScanDevice().cancelCaptureImage();
                SecondFragment.CaptureInfo tmpInfo = new SecondFragment.CaptureInfo();
                _SetLEDs(tmpInfo, __LED_COLOR_NONE__, false);
                m_nCurrentCaptureStep = -1;
                m_bNeedClearPlaten = false;
                m_bBlank = false;

                _SetStatusBarMessage("Secuencia de captura abortada");
                m_strImageMessage = "";
                _SetImageMessage("");
                OnMsg_UpdateDisplayResources();
            } catch (IBScanException ibse) {
                _SetStatusBarMessage("cancel returned exception " + ibse.getType().toString() + ".");
            }
        }
    };


    /*
     * Handlehaga clic en el spinner que determina los dispositivos USB.
     */
    private AdapterView.OnItemSelectedListener m_cboUsbDevicesItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(final AdapterView<?> parent, final View view, final int pos,
                                   final long id) {
            OnMsg_cboUsbDevice_Changed();
            m_savedData.usbDevices = pos;
        }

        @Override
        public void onNothingSelected(final AdapterView<?> parent) {
            m_savedData.usbDevices = __INVALID_POS__;
        }
    };

    /*
     * Handle haga clic en el spinner que determina la captura de la Huella digital.
     */
    private AdapterView.OnItemSelectedListener m_captureTypeItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(final AdapterView<?> parent, final View view, final int pos,
                                   final long id) {
            if (pos == 0) {
                m_btnCaptureStart.setEnabled(false);
            } else {
                m_btnCaptureStart.setEnabled(true);
            }

            m_savedData.captureSeq = pos;
        }

        @Override
        public void onNothingSelected(final AdapterView<?> parent) {
            m_savedData.captureSeq = __INVALID_POS__;
        }
    };

    /*
     * Ocultar el cuadro de diálogo ampliado, si existe.
     */
    private View.OnClickListener m_btnCloseEnlargedDialogClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            if (m_enlargedDialog != null) {
                m_enlargedDialog.cancel();
                m_enlargedDialog = null;
            }
        }
    };

    // //////////////////////////////////////////////////////////////////////////////////////////////
    // PUBLIC INTERFACE: IBScanListener METHODS
    // //////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void scanDeviceAttached(final int deviceId) {
        showToastOnUiThread("Dispositivo " + deviceId + " adjunto", Toast.LENGTH_SHORT);

        /*
         Verifique si tenemos permiso para acceder a este dispositivo.
         Solicite permiso para que aparezca como un escáner de IB.
         */
        final boolean hasPermission = m_ibScan.hasPermission(deviceId);
        if (!hasPermission) {
            m_ibScan.requestPermission(deviceId);
        }
    }

    @Override
    public void scanDeviceDetached(final int deviceId) {
        /*
         Un dispositivo ha sido desconectado. También deberíamos recibir
         una devolución de llamada scanDeviceCountChanged (), donde podemos actualizar la pantalla.
         Si nuestro dispositivo se ha desconectado durante el escaneo,
         también deberíamos recibir una devolución de llamada de DeviceCommunicationBreak ().
         */
        showToastOnUiThread("Dispositivo " + deviceId + " separado", Toast.LENGTH_SHORT);
    }

    @Override
    public void scanDevicePermissionGranted(final int deviceId, final boolean granted) {
        if (granted) {
            /*
            Este dispositivo debe aparecer como un escáner de IB. Podemos esperar por el
            devolución de llamada ScanDeviceCountChanged () para actualizar la pantalla.
             */
            showToastOnUiThread("Permiso otorgado al dispositivo " + deviceId, Toast.LENGTH_SHORT);
        } else {
            showToastOnUiThread("Permiso denegado al dispositivo " + deviceId, Toast.LENGTH_SHORT);
        }
    }

    @Override
    public void scanDeviceCountChanged(final int deviceCount) {
        OnMsg_UpdateDeviceList(false);
    }

    @Override
    public void scanDeviceInitProgress(final int deviceIndex, final int progressValue) {
        OnMsg_SetStatusBarMessage("Inicializando el dispositivo..." + progressValue + "%");
    }

    @Override
    public void scanDeviceOpenComplete(final int deviceIndex, final IBScanDevice device,
                                       final IBScanException exception) {
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////
    // PUBLIC INTERFACE: IBScanDeviceListener METHODS
    // //////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void deviceCommunicationBroken(final IBScanDevice device) {
        OnMsg_DeviceCommunicationBreak();
    }

    @Override
    public void deviceImagePreviewAvailable(final IBScanDevice device, final IBScanDevice.ImageData image) {
        OnMsg_DrawImage(device, image);
    }

    @Override
    public void deviceFingerCountChanged(final IBScanDevice device, final IBScanDevice.FingerCountState fingerState) {
        if (m_nCurrentCaptureStep >= 0) {
            SecondFragment.CaptureInfo info = m_vecCaptureSeq.elementAt(m_nCurrentCaptureStep);
            if (fingerState == IBScanDevice.FingerCountState.NON_FINGER) {
                _SetLEDs(info, __LED_COLOR_RED__, true);
            } else {
                _SetLEDs(info, __LED_COLOR_YELLOW__, true);
            }
        }
    }

    @Override
    public void deviceFingerQualityChanged(final IBScanDevice device, final IBScanDevice.FingerQualityState[] fingerQualities) {
        for (int i = 0; i < fingerQualities.length; i++) {
            m_FingerQuality[i] = fingerQualities[i];
        }

        OnMsg_DrawFingerQuality();
    }

    @Override
    public void deviceAcquisitionBegun(final IBScanDevice device, final IBScanDevice.ImageType imageType) {
        if (imageType.equals(IBScanDevice.ImageType.ROLL_SINGLE_FINGER)) {
            OnMsg_Beep(__BEEP_OK__);
            m_strImageMessage = "Cuando termine, quite el dedo del sensor";
            _SetImageMessage(m_strImageMessage);
            _SetStatusBarMessage(m_strImageMessage);
        }
    }

    @Override
    public void deviceAcquisitionCompleted(final IBScanDevice device, final IBScanDevice.ImageType imageType) {
        if (imageType.equals(IBScanDevice.ImageType.ROLL_SINGLE_FINGER)) {
            OnMsg_Beep(__BEEP_OK__);
        } else {
            OnMsg_Beep(__BEEP_SUCCESS__);
            _SetImageMessage("Retire los dedos del sensor");
            _SetStatusBarMessage("Adquisición completada, posprocesamiento..");
        }
    }

    @Override
    public void deviceImageResultAvailable(final IBScanDevice device, final IBScanDevice.ImageData image,
                                           final IBScanDevice.ImageType imageType, final IBScanDevice.ImageData[] splitImageArray) {
        /* TODO: ALTERNATIVELY, USE RESULTS IN THIS FUNCTION */
        Bitmap bm = image.toBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
        byte[] b = baos.toByteArray();

        String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);

        try{
            MessageDigest md = MessageDigest.getInstance( "SHA-256" );

            // Change this to UTF-16 if needed
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                md.update( encodedImage.getBytes( StandardCharsets.UTF_8 ) );
            }
            byte[] digest = md.digest();

            fingerPrintHash = String.format( "%064x", new BigInteger( 1, digest ) );
            Log.d("imagen", encodedImage);
        } catch ( NoSuchAlgorithmException ex ){
            showToastOnUiThread(ex.getMessage(),Toast.LENGTH_SHORT);
        }
    }

    @Override
    public void deviceImageResultExtendedAvailable(IBScanDevice device, IBScanException imageStatus,
                                                   final IBScanDevice.ImageData image, final IBScanDevice.ImageType imageType, final int detectedFingerCount,
                                                   final IBScanDevice.ImageData[] segmentImageArray, final IBScanDevice.SegmentPosition[] segmentPositionArray) {

        m_savedData.imagePreviewImageClickable = true;
        m_imgPreview.setLongClickable(true);
        m_lastResultImage = image;
        m_lastSegmentImages = segmentImageArray;

        // El valor de imageStatus es mayor que "STATUS_OK", adquisición de imagen exitosa.
        if (imageStatus == null /*STATUS_OK*/ ||
                imageStatus.getType().compareTo(IBScanException.Type.INVALID_PARAM_VALUE) > 0) {
            if (imageType.equals(IBScanDevice.ImageType.ROLL_SINGLE_FINGER)) {
                OnMsg_Beep(__BEEP_SUCCESS__);
            }
        }

        if (m_bNeedClearPlaten) {
            m_bNeedClearPlaten = false;
            OnMsg_DrawFingerQuality();
        }

        // El valor de imageStatus es mayor que "STATUS_OK", adquisición de imagen exitosa.
        if (imageStatus == null /*STATUS_OK*/ ||
                imageStatus.getType().compareTo(IBScanException.Type.INVALID_PARAM_VALUE) > 0) {
            // Adquisición de imagen exitosa
            SecondFragment.CaptureInfo info = m_vecCaptureSeq.elementAt(m_nCurrentCaptureStep);
            _SetLEDs(info, __LED_COLOR_GREEN__, false);

            // Guardar imagen
/*			if (m_chkSaveImages.isSelected())
			{
				// Show chooser for output image.
				JFileChooser chooser = new JFileChooser();
				chooser.setFileFilter(imageFilter);
				int returnVal = chooser.showSaveDialog(IBScanUltimate_Sample.this);

				if (returnVal == JFileChooser.APPROVE_OPTION)
				{
					_SetStatusBarMessage("Saving image...");
					m_ImgSaveFolderName = chooser.getCurrentDirectory().toString() + File.separator + chooser.getSelectedFile().getName();
					_SaveBitmapImage(image, info.fingerName);
					_SaveWsqImage(image, info.fingerName);
					_SavePngImage(image, info.fingerName);
					_SaveJP2Image(image, info.fingerName);

					//save segmented fingers
					for (int i = 0; i < detectedFingerCount; i++)
				{
						String segmentName = info.fingerName + "_Segment_" + String.valueOf(i);
						_SaveBitmapImage(segmentImageArray[i], segmentName);
						_SaveWsqImage(segmentImageArray[i], segmentName);
						_SavePngImage(segmentImageArray[i], segmentName);
						_SaveJP2Image(segmentImageArray[i], segmentName);
				}
			}
			}
*/
//			if (m_chkDrawSegmentImage.isSelected())
            {
                m_nSegmentImageArrayCount = detectedFingerCount;
                m_SegmentPositionArray = segmentPositionArray;
            }

            // NFIQ
//			if (m_chkNFIQScore.isSelected())
            {
                byte[] nfiq_score = {0, 0, 0, 0};
                try {
                    for (int i = 0, segment_pos = 0; i < 4; i++) {
                        if (m_FingerQuality[i].ordinal() != IBScanDevice.FingerQualityState.FINGER_NOT_PRESENT.ordinal()) {
                            nfiq_score[i] = (byte) getIBScanDevice().calculateNfiqScore(segmentImageArray[segment_pos++]);
                        }
                    }
                } catch (IBScanException ibse) {
                    ibse.printStackTrace();
                }

            }

            if (imageStatus == null /*STATUS_OK*/) {
                m_strImageMessage = "Adquisición completada con éxito";
                _SetImageMessage(m_strImageMessage);
                _SetStatusBarMessage(m_strImageMessage);
            } else {
                // > IBSU_STATUS_OK
                m_strImageMessage = "Adquisición de advertencia (código de advertencia = " + imageStatus.getType().toString() + ")";
                _SetImageMessage(m_strImageMessage);
                _SetStatusBarMessage(m_strImageMessage);

                OnMsg_DrawImage(device, image);
                OnMsg_AskRecapture(imageStatus);
                return;
            }
        } else {
            // < IBSU_STATUS_OK
            m_strImageMessage = "Adquisición fallida (Código de error = " + imageStatus.getType().toString() + ")";
            _SetImageMessage(m_strImageMessage);
            _SetStatusBarMessage(m_strImageMessage);

            // Detener toda adquisición
            m_nCurrentCaptureStep = (int) m_vecCaptureSeq.size();
        }

        OnMsg_DrawImage(device, image);

        OnMsg_CaptureSeqNext();
    }

    @Override
    public void devicePlatenStateChanged(final IBScanDevice device, final IBScanDevice.PlatenState platenState) {
        if (platenState.equals(IBScanDevice.PlatenState.HAS_FINGERS))
            m_bNeedClearPlaten = true;
        else
            m_bNeedClearPlaten = false;

        if (platenState.equals(IBScanDevice.PlatenState.HAS_FINGERS)) {
            m_strImageMessage = "Por favor, retire sus dedos en la platina primero!";
            _SetImageMessage(m_strImageMessage);
            _SetStatusBarMessage(m_strImageMessage);
        } else {
            if (m_nCurrentCaptureStep >= 0) {
                SecondFragment.CaptureInfo info = m_vecCaptureSeq.elementAt(m_nCurrentCaptureStep);

                // Mostrar mensaje para adquisición de imágenes nuevamente
                String strMessage = info.PreCaptureMessage;

                _SetStatusBarMessage(strMessage);
//				if (!m_chkAutoCapture.isSelected())
//					strMessage += "\r\nPress button 'Take Result Image' when image is good!";

                _SetImageMessage(strMessage);
                m_strImageMessage = strMessage;
            }
        }

        OnMsg_DrawFingerQuality();
    }

    @Override
    public void deviceWarningReceived(final IBScanDevice device, final IBScanException warning) {
        _SetStatusBarMessage("Advertencia recibida " + warning.getType().toString());
    }

    @Override
    public void devicePressedKeyButtons(IBScanDevice device, int pressedKeyButtons) {
        _SetStatusBarMessage("PressedKeyButtons " + pressedKeyButtons);

        boolean selectedDev = m_cboUsbDevices.getSelectedItemPosition() > 0;
        boolean idle = m_bInitializing && (m_nCurrentCaptureStep == -1);
        boolean active = m_bInitializing && (m_nCurrentCaptureStep != -1);
        try {
            if (pressedKeyButtons == __LEFT_KEY_BUTTON__) {
                if (selectedDev && idle) {
                    System.out.println("Iniciar captura");
                    device.setBeeper(IBScanDevice.BeepPattern.BEEP_PATTERN_GENERIC, 2/*Sol*/, 4/*100ms = 4*25ms*/, 0, 0);
                    this.m_btnCaptureStart.performClick();
                }
            } else if (pressedKeyButtons == __RIGHT_KEY_BUTTON__) {
                if ((active)) {
                    System.out.println("Detener captura");
                    device.setBeeper(IBScanDevice.BeepPattern.BEEP_PATTERN_GENERIC, 2/*Sol*/, 4/*100ms = 4*25ms*/, 0, 0);
                    this.m_btnCaptureStop.performClick();
                }
            }
        } catch (IBScanException e) {
            e.printStackTrace();
        }
    }
    //endregion
 
}