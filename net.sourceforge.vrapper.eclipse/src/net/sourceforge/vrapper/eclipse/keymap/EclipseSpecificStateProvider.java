package net.sourceforge.vrapper.eclipse.keymap;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafCtrlBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.operatorCmds;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.prefixedOperatorCmds;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.transitionBind;
import static net.sourceforge.vrapper.vim.commands.ConstructorWrappers.dontRepeat;
import static net.sourceforge.vrapper.vim.commands.ConstructorWrappers.seq;
import net.sourceforge.vrapper.eclipse.commands.EclipseShiftOperation;
import net.sourceforge.vrapper.keymap.SpecialKey;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.StateUtils;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.DeselectAllCommand;
import net.sourceforge.vrapper.vim.commands.LeaveVisualModeCommand;
import net.sourceforge.vrapper.vim.commands.TextObject;
import net.sourceforge.vrapper.vim.modes.KeyMapResolver;
import net.sourceforge.vrapper.vim.modes.NormalMode;

/**
 * Provides eclipse-specific bindings for command based modes.
 *
 * @author Matthias Radig
 */
@SuppressWarnings("unchecked")
public class EclipseSpecificStateProvider extends AbstractEclipseSpecificStateProvider {
    
    public EclipseSpecificStateProvider() {
        commands.add("eclipseaction", new EclipseActionEvaluator(false));
        commands.add("eclipseaction!", new EclipseActionEvaluator(true));
    }
    
    protected State<Command> visualModeBindings() {
        Command leaveVisual = LeaveVisualModeCommand.INSTANCE;
        Command shiftRight = new EclipseShiftOperation.Visual(false);
        Command shiftLeft = new EclipseShiftOperation.Visual(true);
        return state(
            transitionBind('g',
                    leafBind('c', seq(editText("toggle.comment"), leaveVisual)), // not in Vim
                    leafBind('U', seq(editText("upperCase"),      leaveVisual)),
                    leafBind('u', seq(editText("lowerCase"),      leaveVisual))),
            leafBind('>', shiftRight),
            leafBind('<', shiftLeft));
    }

    protected State<String> normalModeKeymap() {
        State<String> normalModeKeymap = state(
                        leafBind('z', KeyMapResolver.NO_KEYMAP),
                        leafBind('g', KeyMapResolver.NO_KEYMAP));
        return normalModeKeymap;
    }

    protected State<String> visualModeKeymap() {
        return state(leafBind('g', KeyMapResolver.NO_KEYMAP));
    }

    protected State<Command> normalModeBindings() {
        State<TextObject> textObjects = NormalMode.textObjects();
        State<Command> normalModeBindings = StateUtils.union(
            state(
                leafBind('J', (Command) editText("join.lines")),
                transitionBind('z',
                        leafBind('o', dontRepeat(editText("folding.expand"))),
                        leafBind('R', dontRepeat(editText("folding.expand_all"))),
                        leafBind('c', dontRepeat(editText("folding.collapse"))),
                        leafBind('M', dontRepeat(editText("folding.collapse_all")))),
                transitionBind('g',
                        leafBind('t', cmd("org.eclipse.ui.window.nextEditor")),
                        leafBind('T', cmd("org.eclipse.ui.window.previousEditor"))),
                leafCtrlBind('f', go("pageDown")),
                leafCtrlBind('b', go("pageUp")),
                leafBind(SpecialKey.PAGE_DOWN, go("pageDown")),
                leafBind(SpecialKey.PAGE_UP, go("pageUp")),
                leafCtrlBind('y', dontRepeat(editText("scroll.lineUp"))),
                leafCtrlBind('e', dontRepeat(editText("scroll.lineDown"))),
                leafCtrlBind('i', dontRepeat(cmd("org.eclipse.ui.navigate.forwardHistory"))),
                leafCtrlBind('o', dontRepeat(cmd("org.eclipse.ui.navigate.backwardHistory")))),
            prefixedOperatorCmds('g', 'u', seq(editText("lowerCase"), DeselectAllCommand.INSTANCE), textObjects),
            prefixedOperatorCmds('g', 'U', seq(editText("upperCase"), DeselectAllCommand.INSTANCE), textObjects),
            operatorCmds('>', new EclipseShiftOperation.Normal(false), textObjects),
            operatorCmds('<', new EclipseShiftOperation.Normal(true), textObjects)
         );
        return normalModeBindings;
    }

}