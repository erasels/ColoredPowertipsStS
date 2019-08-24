package coloredPowertips.patches;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.helpers.TipHelper;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;
import javassist.*;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ColoredPowerPowertips {
    private static final String BUFF_TEXT = "\u200D";
    private static final String DEBUFF_TEXT = "\u200B";
    private static final int TEXT_LENGTH = BUFF_TEXT.length();
    public static Color BUFF_COL;
    public static Color DEBUFF_COL;
    private static final HashMap<String, Integer> powerMap = new HashMap<>(); //1 = Buff, 2 = Debuff

    @SpirePatch(
            clz = TipHelper.class,
            method = "renderTipBox"
    )
    public static class ColorChanger {
        @SpireInsertPatch(locator = Locator.class, localvars = {"description"})
        public static void patch(float x, float y, SpriteBatch sb, String titl, String descriptio, @ByRef String[] description) {
            if (description[0] != null) {
                if (description[0].startsWith(BUFF_TEXT)) {
                    description[0] = description[0].substring(TEXT_LENGTH);
                    sb.setColor(BUFF_COL);
                } else if (description[0].startsWith(DEBUFF_TEXT)) {
                    description[0] = description[0].substring(TEXT_LENGTH);
                    sb.setColor(DEBUFF_COL);
                }
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
                    try {
                        i = getTypeHyperBrain(__instance, pT.header);
                    } catch (Exception e) {
                        i = getType(getPowerByName(__instance, pT.header));
                        e.printStackTrace();
                    }

                    if (i == 1) {
                        pT.body = BUFF_TEXT + pT.body;
                    } else if (i == 2) {
                        pT.body = DEBUFF_TEXT + pT.body;
                    } else if (i == 3) {
                        AbstractPower p = getPowerByName(__instance, pT.header);
                        if (p != null) {
                            if (p.type == AbstractPower.PowerType.BUFF) {
                                pT.body = BUFF_TEXT + pT.body;
                            } else {
                                pT.body = DEBUFF_TEXT + pT.body;
                            }
                        }
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

    private static int getType(AbstractPower p) {
        if (p != null) {
            if (powerMap.containsKey(p.name)) {
                return powerMap.get(p.name);
            }

            int i = p.type == AbstractPower.PowerType.BUFF ? 1 : 2;
            powerMap.put(p.name, i);
            return i;
        }
        return 0; // error code
    }

    private static int pTypesWrites = 0;

    private static int getTypeHyperBrain(AbstractCreature t, String name) throws NotFoundException, CannotCompileException {
        if (powerMap.containsKey(name)) {
            return powerMap.get(name);
        }
        AbstractPower p = getPowerByName(t, name);
        if (p != null) {
            pTypesWrites = 0;

            ClassPool pool = Loader.getClassPool();
            CtClass ctClass = pool.get(p.getClass().getName());

            do {
                ctClass.instrument(new ExprEditor() {
                    @Override
                    public void edit(FieldAccess f) {

                        if (f.getFieldName().equals("type") && f.isWriter()) {
                            pTypesWrites++;
                        }

                    }
                });
                ctClass = ctClass.getSuperclass();
            } while (!ctClass.getName().equals(AbstractPower.class.getName()));

            int i;
            if (pTypesWrites < 2) {
                i = getType(p);
            } else {
                i = 3;
            }

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
