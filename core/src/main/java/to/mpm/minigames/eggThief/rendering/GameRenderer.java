package to.mpm.minigames.eggThief.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.IntMap;
import to.mpm.minigames.eggThief.entities.Duck;
import to.mpm.minigames.eggThief.entities.Egg;
import to.mpm.minigames.eggThief.entities.Nest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GameRenderer {
    private static BitmapFont font;
    private static BitmapFont subtitleFont;
    private static GlyphLayout layout;

    public static void initialize() {
        if (font == null) {
            try {
                Skin skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
                font = skin.getFont("font");
                font.getData().setScale(1.5f);
                subtitleFont = new BitmapFont();
                subtitleFont.getData().setScale(2f);
                layout = new GlyphLayout();
            } catch (Exception e) {
                font = new BitmapFont();
                font.getData().setScale(2f);
                subtitleFont = new BitmapFont();
                subtitleFont.getData().setScale(2.5f);
                layout = new GlyphLayout();
                Gdx.app.log("GameRenderer", "Using default font (skin not found)");
            }
        }
    }

    public static void render(SpriteBatch batch, ShapeRenderer shapeRenderer,
            IntMap<Duck> players, List<Egg> eggs, List<Nest> nests,
            Map<Integer, Integer> scores, int localPlayerId,
            float gameTimer) {

        // DRAW FILLED SHAPES
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Dibuja los nidos
        for (Nest nest : nests) {
            shapeRenderer.setColor(nest.r, nest.g, nest.b, 1f);
            float[] vertices = {
                    nest.getX(), nest.getY(),
                    nest.getX() + Nest.NEST_SIZE, nest.getY(),
                    nest.getX() + Nest.NEST_SIZE / 2, nest.getY() + Nest.NEST_SIZE
            };
            shapeRenderer.triangle(vertices[0], vertices[1], vertices[2],
                    vertices[3], vertices[4], vertices[5]);
        }

        // Dibuja los huevos en el suelo
        for (Egg egg : eggs) {
            shapeRenderer.setColor(egg.isGolden ? 1f : 1f,
                    egg.isGolden ? 0.84f : 1f,
                    egg.isGolden ? 0f : 1f, 1f);
            shapeRenderer.circle(egg.x, egg.y, Egg.EGG_SIZE / 2);
        }

        // Dibuja los patos y su huevo llevado
        for (IntMap.Entry<Duck> entry : players) {
            Duck duck = entry.value;
            shapeRenderer.setColor(duck.r, duck.g, duck.b, 1f);
            shapeRenderer.rect(duck.x, duck.y, Duck.DUCK_SIZE, Duck.DUCK_SIZE);

            Egg carriedEgg = duck.getCarriedEgg();
            if (carriedEgg != null) {
                shapeRenderer.setColor(carriedEgg.isGolden ? 1f : 1f,
                        carriedEgg.isGolden ? 0.84f : 1f,
                        carriedEgg.isGolden ? 0f : 1f, 1f);
                shapeRenderer.circle(duck.x + Duck.DUCK_SIZE / 2,
                        duck.y + Duck.DUCK_SIZE + Egg.EGG_SIZE / 2,
                        Egg.EGG_SIZE / 2);
            }
        }

        // HUD Background
        shapeRenderer.setColor(0, 0, 0, 0.5f);
        shapeRenderer.rect(0, 480 - 50, 640, 50);

        shapeRenderer.end();

        // 2. DRAW LINES
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        // Outline de patos
        for (IntMap.Entry<Duck> entry : players) {
            Duck duck = entry.value;
            shapeRenderer.setColor(0f, 0f, 0f, 1f);
            shapeRenderer.rect(duck.x, duck.y, Duck.DUCK_SIZE, Duck.DUCK_SIZE);
        }

        shapeRenderer.end();

        // -------------------------------
        // 3. DRAW TEXT (SpriteBatch)
        // -------------------------------
        batch.begin();

        // Timer centrado en la parte superior
        if (subtitleFont != null) {
            int minutes = (int) gameTimer / 60;
            int seconds = (int) gameTimer % 60;
            String timeString = String.format("%02d:%02d", minutes, seconds);
            layout.setText(subtitleFont, timeString);
            subtitleFont.draw(batch, timeString, (640 - layout.width) / 2, 480 - 15);
        }

        batch.end();

        // Render ranking de jugadores (izquierda superior)
        renderPlayerRanking(batch, shapeRenderer, players, scores, localPlayerId);

        // Render puntaje del jugador local (derecha superior)
        batch.begin();
        if (font != null) {
            Integer localPlayerScore = scores.get(localPlayerId);
            if (localPlayerScore != null) {
                String localScoreString = localPlayerScore + " pts";
                layout.setText(font, localScoreString);
                font.draw(batch, localScoreString, 640 - layout.width - 10, 480 - 15);
            }
        }
        batch.end();
    }

    /**
     * Renderiza el ranking de jugadores ordenado por puntaje en la izquierda
     * superior.
     */
    private static void renderPlayerRanking(SpriteBatch batch, ShapeRenderer shapeRenderer,
            IntMap<Duck> players, Map<Integer, Integer> scores, int localPlayerId) {
        if (font == null)
            return;

        // Ordenar jugadores por puntaje (mayor a menor)
        List<Map.Entry<Integer, Integer>> sortedScores = new ArrayList<>(scores.entrySet());
        sortedScores.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        float startX = 10;
        float startY = 480 - 60;
        float boxSize = 20;
        float spacing = 25;
        float textOffsetX = 28;

        int yOffset = 0;
        for (Map.Entry<Integer, Integer> scoreEntry : sortedScores) {
            int playerId = scoreEntry.getKey();
            Duck duck = players.get(playerId);
            if (duck == null)
                continue;

            float y = startY - yOffset;

            // Color box
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(duck.r, duck.g, duck.b, 1f);
            shapeRenderer.rect(startX, y - boxSize, boxSize, boxSize);
            shapeRenderer.end();

            // Outline
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
            shapeRenderer.rect(startX, y - boxSize, boxSize, boxSize);
            shapeRenderer.end();

            // Score text
            batch.begin();
            String scoreString = ": " + scoreEntry.getValue();

            // Highlight local player
            if (playerId == localPlayerId) {
                font.setColor(1f, 1f, 0.3f, 1f);
            } else {
                font.setColor(1f, 1f, 1f, 1f);
            }

            font.draw(batch, scoreString, startX + textOffsetX, y - boxSize / 4);
            batch.end();

            yOffset += spacing;
        }
    }

    public static void dispose() {
        if (font != null)
            font.dispose();
        if (subtitleFont != null)
            subtitleFont.dispose();
    }
}
