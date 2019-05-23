package coloredPowertips;

import basemod.*;
import basemod.interfaces.*;

import coloredPowertips.patches.ColoredPowerPowertips;
import com.badlogic.gdx.graphics.Color;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.localization.UIStrings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

@SpireInitializer
public class ColoredPowertips implements PostInitializeSubscriber, StartGameSubscriber {
    private static SpireConfig modConfig = null;

    public static void initialize() {
        BaseMod.subscribe(new ColoredPowertips());
        try {
            Properties defaults = new Properties();
            defaults.put("color", Integer.toString(0));
            modConfig = new SpireConfig("ColoredPowertips", "Config", defaults);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int colorChoice() {
        if (modConfig == null) {
            return 0;
        }
        return modConfig.getInt("color");
    }

    @Override
    public void receivePostInitialize() {
        System.out.println("Powertips are now colored, have fun :D");

        //UIStrings buttonStrings = CardCrawlGame.languagePack.getUIString("coloredpowertips:options");
        //String[] TEXT = buttonStrings.TEXT;

        ModPanel settingsPanel = new ModPanel();
        ModLabel text = new ModLabel("Do not believe the checkmarks! The option that was last clicked is the one that will be used.", 350, 700, Color.SALMON, settingsPanel, click->{});
        settingsPanel.addUIElement(text);
        ModLabeledToggleButton GRBtn = new ModLabeledToggleButton("Green/Red", 350, 650, Settings.GREEN_TEXT_COLOR, FontHelper.charDescFont, colorChoice() == 0, settingsPanel, l -> {
        },
                button ->
                {
                    if (modConfig != null) {
                        modConfig.setInt("color", 0);
                        try {
                            modConfig.save();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
        settingsPanel.addUIElement(GRBtn);

        ModLabeledToggleButton BOBtn = new ModLabeledToggleButton("Blue/Orange", 350, 600, Settings.BLUE_TEXT_COLOR, FontHelper.charDescFont, colorChoice() == 1, settingsPanel, l -> {
        },
                button ->
                {
                    if (modConfig != null) {
                        modConfig.setInt("color", 1);
                        try {
                            modConfig.save();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
        settingsPanel.addUIElement(BOBtn);

        BaseMod.registerModBadge(ImageMaster.loadImage("ColoredPowertipsResources/img/modBadge.png"), "ColoredPowertips", "erasels", "TODO", settingsPanel);
    }

    @Override
    public void receiveStartGame() {
        switch (colorChoice()) {
            case 1:
                ColoredPowerPowertips.BUFF_COL = new Color(Color.SKY.cpy());
                ColoredPowerPowertips.DEBUFF_COL = new Color((float) 244 / 255.0F, (float) 162 / 255.0F, (float) 85 / 255.0F, (float) 255 / 255.0F);
                break;
            default:
                ColoredPowerPowertips.BUFF_COL = new Color((float) 106 / 255.0F, (float) 206 / 255.0F, (float) 125 / 255.0F, (float) 255 / 255.0F);
                ColoredPowerPowertips.DEBUFF_COL = new Color(Color.SALMON.cpy());
        }
    }
}
