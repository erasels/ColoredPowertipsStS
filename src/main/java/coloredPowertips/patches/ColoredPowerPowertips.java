package coloredPowertips.patches;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.helpers.TipHelper;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;
import javassist.CannotCompileException;
import javassist.CtBehavior;

import java.util.ArrayList;
import java.util.HashMap;

public class ColoredPowerPowertips {
    private static final String BUFF_TEXT = "@gkB@";
    private static final String DEBUFF_TEXT = "@gkD@";
    private static final Color BUFF_GREEN = new Color((float) 106 / 255.0F, (float) 206 / 255.0F, (float) 125 / 255.0F, (float) 255 / 255.0F);
    private static final HashMap<String, Integer> powerMap = new HashMap<>(); //1 = Buff, 2 = Debuff

    @SpirePatch(
            clz = TipHelper.class,
            method = "renderTipBox"
    )
    public static class ColorChanger {
        @SpireInsertPatch(locator = Locator.class, localvars = {"title"})
        public static void patch(float x, float y, SpriteBatch sb, String titl, String description, @ByRef String[] title) {
            if (title[0].startsWith(BUFF_TEXT)) {
                title[0] = title[0].substring(5);
                sb.setColor(BUFF_GREEN);
            } else if (title[0].startsWith(DEBUFF_TEXT)) {
                title[0] = title[0].substring(5);
                sb.setColor(Color.SALMON);
            }
        }

        private static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(SpriteBatch.class, "draw");
                return new int[]{LineFinder.findAllInOrder(ctMethodToPatch, finalMatcher)[3]};
            }
        }
    }

    @SpirePatch(clz = AbstractMonster.class, method = "renderTip")
    @SpirePatch(clz = AbstractCreature.class, method = "renderPowerTips")
    public static class ManipulateTitles {
        @SpireInsertPatch(locator = Locator.class, localvars = {"tips"})
        public static void Insert(AbstractCreature __instance, SpriteBatch sb, @ByRef ArrayList<PowerTip>[] tips) {
            int i;
            for (PowerTip pT : tips[0]) {
                if (pT.header != null) {
                    i = getType(__instance, pT.header);
                    if (i == 1) {
                        pT.header = BUFF_TEXT + pT.header;
                    } else if (i == 2) {
                        pT.header = DEBUFF_TEXT + pT.header;
                    }
                }
            }
        }

        private static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(ArrayList.class, "isEmpty");
                return LineFinder.findInOrder(ctMethodToPatch, new ArrayList<>(), finalMatcher);
            }
        }
    }

    private static int getType(AbstractCreature t, String name) {
        if (powerMap.containsKey(name)) {
            return powerMap.get(name);
        }
        AbstractPower p = getPowerByName(t, name);
        if (p != null) {
            int i = p.type == AbstractPower.PowerType.BUFF ? 1 : 2;
            powerMap.put(name, i);
            return i;
        }
        return 0; // error code
    }

    private static AbstractPower getPowerByName(AbstractCreature t, String name) {
        for (AbstractPower p : t.powers) {
            if (name.equals(p.name)) {
                return p;
            }
        }
        return null;
    }
}

/*public static class ManipulateTitles {
        @SpireInsertPatch(locator = Locator.class, localvars = {"tips"})
        public static void Insert(AbstractCreature __instance, SpriteBatch sb, @ByRef ArrayList<PowerTip>[] tips) {
            for (PowerTip pT : tips[0]) {
                if (pT.header != null) {
                    //AbstractPower p = getPowerByName(__instance, pT.header);
                    //if (p != null) {
                        if (p.type == AbstractPower.PowerType.BUFF) {
                            pT.header = BUFF_TEXT + pT.header;
                        } else {
                            pT.header = DEBUFF_TEXT + pT.header;
                        }
                        //RiskOfSpire.logger.info("inside: " + pT.header);
                    //}
                }
            }
        }*/
