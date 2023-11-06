package com.example.lab6_20192858;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import com.example.lab6_20192858.Adapter.ImageDialogAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class PuzzleActivity extends AppCompatActivity {
    private boolean isGameStarted = false; // Paso 2

    Button uploadImageButton, iniciarJuegoButton;
    GridView puzzleGridView;
    ImageDialogAdapter imageAdapter;
    ActivityResultLauncher<Intent> imagePickerLauncher;
    private int gridSize;
    private int blankPos;
    private ArrayList<Bitmap> puzzlePieces;
    private ArrayList<Bitmap> initialPuzzleState;

    private boolean isResetting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle);

        uploadImageButton = findViewById(R.id.SubirImagen);
        iniciarJuegoButton = findViewById(R.id.IniciarJuego);
        iniciarJuegoButton.setEnabled(false);

        iniciarJuegoButton.setOnClickListener(v -> {
            if (!isGameStarted) {
                isGameStarted = true;
                isResetting = true; 
                shufflePuzzle();
                iniciarJuegoButton.setText("Resetear");
                isResetting = false;
            } else {
                isResetting = true;
                isGameStarted = false;
                puzzlePieces = new ArrayList<>(initialPuzzleState);
                iniciarJuegoButton.setText("Iniciar Juego");
                isResetting = false;
            }
            imageAdapter.updatePuzzlePieces(puzzlePieces);
            imageAdapter.notifyDataSetChanged();
        });
        puzzleGridView = findViewById(R.id.puzzleGridView);
        imageAdapter = new ImageDialogAdapter(this, new ArrayList<>());
        puzzleGridView.setAdapter(imageAdapter);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getData() != null) {
                            Uri selectedImageUri = data.getData();
                            Log.d("PuzzleActivity", "Imagen seleccionada: " + selectedImageUri.toString());
                            setUpPuzzle(selectedImageUri);
                        } else {
                            Log.d("PuzzleActivity", "No se seleccionó ninguna imagen o falta de datos");
                        }
                        iniciarJuegoButton.setEnabled(true);
                    } else {
                        Log.d("PuzzleActivity", "Resultado no es OK");
                    }
                }
        );
        puzzleGridView.setOnItemClickListener((parent, view, position, id) -> {
            if (isGameStarted && isValidMove(position)) {
                Log.d("PuzzleActivity", "isValidMove - Posición seleccionada: " + position + ", Posición en blanco: " + blankPos);

                swapPieces(position);
                imageAdapter.updatePuzzlePieces(puzzlePieces);
                imageAdapter.notifyDataSetChanged();
            }
        });

        uploadImageButton.setOnClickListener(v -> {
            Log.d("PuzzleActivity", "Botón SubirImagen presionado");
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });
    }
    private boolean isValidMove(int position) {
        int blankRow = blankPos / gridSize;
        int blankCol = blankPos % gridSize;
        int clickedRow = position / gridSize;
        int clickedCol = position % gridSize;
        return (blankRow == clickedRow && Math.abs(blankCol - clickedCol) == 1) ||
                (blankCol == clickedCol && Math.abs(blankRow - clickedRow) == 1);
    }
    private void swapPieces(int position) {
        Log.d("PuzzleActivity", "swapPieces - Posición seleccionada: " + position + ", Posición en blanco: " + blankPos);

        Bitmap clickedPiece = puzzlePieces.get(position);
        Bitmap blankPiece = puzzlePieces.get(blankPos);

        puzzlePieces.set(blankPos, clickedPiece);
        puzzlePieces.set(position, blankPiece);

        blankPos = position;
        Log.d("PuzzleActivity", "swapPieces - Piezas intercambiadas. Nueva posición en blanco: " + blankPos);

        if (!isResetting && isPuzzleSolved()) {
            playWinSound();
            Toast.makeText(this, "¡Ganaste el juego!", Toast.LENGTH_LONG).show();
            iniciarJuegoButton.setText("Iniciar Juego");
            isGameStarted = false;
        }
    }
    private boolean isPuzzleSolved() {
        for (int i = 0; i < puzzlePieces.size(); i++) {
            if (!isCorrectPosition(puzzlePieces.get(i), i)) {
                return false;
            }
        }
        return true;
    }

    private boolean isCorrectPosition(Bitmap piece, int position) {

        return piece.equals(initialPuzzleState.get(position));
    }

    // Esta es una nueva función para mezclar el puzzle de manera controlada
    private void shufflePuzzle() {
        Random random = new Random();
        int moves = 0;
        while (moves < 100) {
            ArrayList<Integer> validMoves = getValidMoves();
            if (!validMoves.isEmpty()) {
                swapPieces(validMoves.get(random.nextInt(validMoves.size())));
                moves++;
            }
        }
    }

    private ArrayList<Integer> getValidMoves() {
        ArrayList<Integer> validMoves = new ArrayList<>();
        int blankRow = blankPos / gridSize;
        int blankCol = blankPos % gridSize;
        if (blankRow > 0) validMoves.add(blankPos - gridSize);
        if (blankRow < gridSize - 1) validMoves.add(blankPos + gridSize);
        if (blankCol > 0) validMoves.add(blankPos - 1);
        if (blankCol < gridSize - 1) validMoves.add(blankPos + 1);

        return validMoves;
    }
    private void playWinSound() {
            MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.game_win_sound);
        mediaPlayer.setOnCompletionListener(mp -> mp.release());
        mediaPlayer.start();
    }
    private void setUpPuzzle(Uri imageUri) {
        try {
            Bitmap puzzleImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            this.gridSize = new Random().nextInt(3) + 3;
            int spacing = (int) (2 * getResources().getDisplayMetrics().density * (gridSize - 1));
            int width = puzzleGridView.getWidth() - spacing;
            int height = puzzleGridView.getHeight() - spacing;

            int pieceWidth = width / gridSize;
            int pieceHeight = height / gridSize;

            Bitmap scaledImage = Bitmap.createScaledBitmap(puzzleImage, width, height, true);

            if (puzzlePieces == null) {
                puzzlePieces = new ArrayList<>();
            } else {
                puzzlePieces.clear();
            }

            for (int y = 0; y < gridSize; y++) {
                for (int x = 0; x < gridSize; x++) {
                    if (x == gridSize - 1 && y == gridSize - 1) {
                        Bitmap blankBitmap = Bitmap.createBitmap(pieceWidth, pieceHeight, Bitmap.Config.ARGB_8888);
                        blankBitmap.eraseColor(Color.WHITE);
                        puzzlePieces.add(blankBitmap);
                    } else {
                        int posX = x * pieceWidth;
                        int posY = y * pieceHeight;
                        Bitmap pieceBitmap = Bitmap.createBitmap(scaledImage, posX, posY, pieceWidth, pieceHeight);
                        puzzlePieces.add(pieceBitmap);
                    }
                }
            }

            blankPos = puzzlePieces.size() - 1;

            imageAdapter.updatePuzzlePieces(puzzlePieces);
            imageAdapter.notifyDataSetChanged();
            puzzleGridView.setNumColumns(gridSize);
            initialPuzzleState = new ArrayList<>(puzzlePieces); // Paso importante

        } catch (IOException e) {
            Log.e("PuzzleActivity", "Error al cargar imagen: " + e.getMessage());
        }
    }
}
