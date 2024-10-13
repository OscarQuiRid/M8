package com.example.practica02conversor;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class MainActivity2 extends AppCompatActivity {

    private Spinner spinnerFrom, spinnerTo;
    private EditText insertNumbers;
    private TextView tvResultado;
    private Button buttonReturn, buttonClean;

    private int selectedCategoryIndex = 0;

    // Factores de conversión entre las unidades
    private final double[][] conversionFactors = {
            {0.001, 1, 1000, 1e6}, // Peso (mg a demás)
            {1e-3, 1, 1000}, // Volumen (ml a demás)
            {1, 10, 1000, 1e6}, // Distancia (mm a demás)
            {1, 9.0/5.0, 273.15}, // Temperatura (C, F, K)
            {1.0 / 8, 1.0 / 8 / 1024, 1.0 / 8 / 1024 / 1024, 1.0 / 8 / 1024 / 1024 / 1024, 1.0 / 8 / 1024 / 1024 / 1024 / 1024} // Capacidad Disco (bit a demás)
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        selectedCategoryIndex = getIntent().getIntExtra("category_index", 0);
        setupSpinners();
        setupEditText();
        setupButton();
        setupCleanButton();
    }

    private void setupSpinners() {
        spinnerFrom = findViewById(R.id.spinner_primera_unidad);
        spinnerTo = findViewById(R.id.spinner_segunda_unidad);

        // Establecer el adapter para el spinner 1
        ArrayAdapter<CharSequence> adapterFrom = ArrayAdapter.createFromResource(this,
                getUnitsArray(selectedCategoryIndex), android.R.layout.simple_spinner_item);
        adapterFrom.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrom.setAdapter(adapterFrom);
        // Establecer el adapter para el spinner 2
        ArrayAdapter<CharSequence> adapterTo = ArrayAdapter.createFromResource(this,
                getUnitsArray(selectedCategoryIndex), android.R.layout.simple_spinner_item);
        adapterTo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTo.setAdapter(adapterTo);

        // Listener para manejar la selección del spinner 1
        spinnerFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                performConversion();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Listener para manejar la selección del spinner 2
        spinnerTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                performConversion();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupEditText() {
        insertNumbers = findViewById(R.id.insert_numbers);
        tvResultado = findViewById(R.id.tv_resultado);

        insertNumbers.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performConversion();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    // Regresar a la actividad anterior
    private void setupButton() {
        buttonReturn = findViewById(R.id.button_return);
        buttonReturn.setOnClickListener(v -> finish());
    }

    private void setupCleanButton() {
        buttonClean = findViewById(R.id.button_clean);
        buttonClean.setOnClickListener(v -> {
            insertNumbers.setText("");
            tvResultado.setText("");
        });
    }

    private void performConversion() {
        String input = insertNumbers.getText().toString();
        if (!input.isEmpty()) {
            double valueFrom = Double.parseDouble(input);
            int fromIndex = spinnerFrom.getSelectedItemPosition();
            int toIndex = spinnerTo.getSelectedItemPosition();

            double result;

            // Calcular el resultado de la conversión basado en la categoría
            if (selectedCategoryIndex == 3) { // Temperatura
                result = convertTemperature(valueFrom, fromIndex, toIndex);
            } else if (selectedCategoryIndex == 4) { // Capacidad de disco
                result = convertDiskCapacity(valueFrom, fromIndex, toIndex);
            } else {
                result = valueFrom * conversionFactors[selectedCategoryIndex][fromIndex] / conversionFactors[selectedCategoryIndex][toIndex];
            }

            // Formatear el resultado y mostrarlo
            tvResultado.setText(formatResult(result));
        } else {
            tvResultado.setText("");
        }
    }

    // Crear una matriz de funciones de conversión
    private double convertTemperature(double valueFrom, int fromIndex, int toIndex) {
        double[][] conversionMatrix = {
                {valueFrom, (valueFrom * 9 / 5) + 32, valueFrom + 273.15}, // C a C, C a F, C a K
                {((valueFrom - 32) * 5 / 9), valueFrom, ((valueFrom - 32) * 5 / 9) + 273.15}, // F a C, F a F, F a K
                {valueFrom - 273.15, ((valueFrom - 273.15) * 9 / 5) + 32, valueFrom} // K a C, K a F, K a K
        };

        return conversionMatrix[fromIndex][toIndex];
    }

    private double convertDiskCapacity(double valueFrom, int fromIndex, int toIndex) {
        return valueFrom * conversionFactors[selectedCategoryIndex][fromIndex] / conversionFactors[selectedCategoryIndex][toIndex];
    }

    //formato para el resultado para hacerlo mas claro.
    private String formatResult(double result) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        symbols.setDecimalSeparator(','); // Usar coma como separador decimal
        symbols.setGroupingSeparator('.'); // Usar punto como separador de miles

        DecimalFormat decimalFormat;

        if (result < 1 && result > 0) {
            decimalFormat = new DecimalFormat("#,##0.######################", symbols);
        } else if (Math.abs(result) >= 1 && Math.abs(result) < 1000) {
            decimalFormat = new DecimalFormat("#,##0.##", symbols);
        } else {
            decimalFormat = new DecimalFormat("#,##0", symbols);
        }
        return decimalFormat.format(result);
}
    // seleccion que unidades necesita mostrar basandose en lo que recibe del mainActivity
    private int getUnitsArray(int categoryIndex) {
        switch (categoryIndex) {
            case 0: return R.array.peso_unidades;
            case 1: return R.array.volumen_unidades;
            case 2: return R.array.distancia_unidades;
            case 3: return R.array.temperatura_unidades;
            case 4: return R.array.capacidad_disco_duro_unidades;
            default: return R.array.peso_unidades;
        }
    }
}
