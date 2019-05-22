package coloredPowertips;

import basemod.BaseMod;
import basemod.interfaces.*;

import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;

@SpireInitializer
public class ColoredPowertips implements PostInitializeSubscriber {

    private static SpireConfig modConfig = null;


    public static void initialize() {
        BaseMod.subscribe(new ColoredPowertips());
    }

    @Override
    public void receivePostInitialize() {
        System.out.println("Powertips are now colored, have fun :D");
    }
}
