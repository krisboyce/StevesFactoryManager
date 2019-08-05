package vswe.stevesfactory.library.gui.widget;

import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.layout.properties.HorizontalAlignment;
import vswe.stevesfactory.library.gui.widget.mixin.*;
import vswe.stevesfactory.utils.RenderingHelper;

import java.util.Collections;
import java.util.List;

import static vswe.stevesfactory.utils.RenderingHelper.*;

public class TextList extends AbstractWidget implements LeafWidgetMixin {

    private List<String> texts;
    private List<String> textView;
    private boolean fitContents = false;

    public HorizontalAlignment textAlignment = HorizontalAlignment.LEFT;

    public TextList(int width, int height, List<String> texts) {
        super(0, 0, width, height);
        this.texts = texts;
        this.textView = Collections.unmodifiableList(texts);
    }

    @Override
    public void render(int mouseX, int mouseY, float particleTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
        int x = getAbsoluteX() + 1;
        int y = getAbsoluteY() + 1;
        int x2 = getAbsoluteXBR() - 1;
        for (String text : texts) {
            switch (textAlignment) {
                case LEFT:
                    fontRenderer().drawString(text, x, y, 0xffffff);
                    break;
                case CENTER:
                    drawTextCenteredHorizontally(text, x, x2, y, 0xffffff);
                    break;
                case RIGHT:
                    fontRenderer().drawString(text, RenderingHelper.getXForAlignedRight(x2, textWidth(text)), y, 0xffffff);
                    break;
            }
            y += fontHeight();
        }
        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }

    public boolean doesFitContents() {
        return fitContents;
    }

    public void setFitContents(boolean fitContents) {
        this.fitContents = fitContents;
    }

    public void editLine(int line, String newText) {
        texts.set(line, newText);
        tryExpand(newText);
    }

    public String getLine(int line) {
        return texts.get(line);
    }

    public void addLine(String newLine) {
        texts.add(newLine);
        tryExpand(newLine);
    }

    public List<String> getTexts() {
        return textView;
    }

    private void tryExpand(String line) {
        if (fitContents) {
            int w = minecraft().fontRenderer.getStringWidth(line);
            setWidth(Math.max(getWidth(), 1 + w + 1));
            setHeight(1 + (fontHeight() + 2) * texts.size() + 1);
        }
    }
}
