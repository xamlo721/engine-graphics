package com.xamlo.core.engine.graphics.components.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class RadioButtonTest {

    @Test
    public void selectingRadioUnchecksOthersInSameGroup() {
        RadioButton a = new RadioButton("a", "g");
        RadioButton b = new RadioButton("b", "g");
        RadioButton c = new RadioButton("c", "g");

        a.getClickListener().onClicked(a);
        assertTrue(a.isChecked());

        b.getClickListener().onClicked(b);
        assertFalse(a.isChecked(), "другой элемент той же группы снимает отметку");
        assertTrue(b.isChecked());

        c.getClickListener().onClicked(c);
        assertFalse(b.isChecked());
        assertTrue(c.isChecked());
    }

    @Test
    public void differentGroupsDoNotInterfere() {
        RadioButton inFirst = new RadioButton("x", "first");
        RadioButton inSecond = new RadioButton("y", "second");

        inFirst.setChecked(true);
        inSecond.setChecked(true);

        assertTrue(inFirst.isChecked(), "группы независимы друг от друга");
        assertTrue(inSecond.isChecked());
    }

    @Test
    public void radioWithoutGroupIsExclusiveToNobody() {
        RadioButton soloA = new RadioButton("solo-a");
        RadioButton soloB = new RadioButton("solo-b");

        soloA.setChecked(true);
        soloB.setChecked(true);

        assertTrue(soloA.isChecked());
        assertTrue(soloB.isChecked(), "без имени группы исключительности нет");
    }

    @Test
    public void setCheckedFalseDoesNotRecheckOthers() {
        RadioButton a = new RadioButton("a", "g2");
        RadioButton b = new RadioButton("b", "g2");

        a.setChecked(true);
        b.setChecked(true);
        assertTrue(b.isChecked());

        b.setChecked(false);
        assertFalse(a.isChecked(), "сброс одного элемента не включает другой автоматически");
        assertFalse(b.isChecked());
    }

    @Test
    public void movingBetweenGroupsKeepsExclusivityInNewOneOnly() {
        RadioButton shared = new RadioButton("s", "old");
        RadioButton oldMate = new RadioButton("o", "old");
        RadioButton newMate = new RadioButton("n", "new");

        oldMate.setChecked(true);
        assertEquals("old", shared.getGroup());
        shared.setGroup("new");
        assertEquals("new", shared.getGroup());

        shared.setChecked(true);
        assertTrue(oldMate.isChecked(), "старая группа не затрагивается после перерегистрации");
        assertFalse(newMate.isChecked(), "в новой группе исключительность работает сразу");

        newMate.setChecked(true);
        assertFalse(shared.isChecked(), "обратный выбор в новой группе снимает отметку у shared");
        assertTrue(oldMate.isChecked(), "операции в новой группе всё ещё не касаются старой");
    }

}
