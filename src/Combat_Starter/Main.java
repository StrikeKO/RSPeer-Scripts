package Combat_Starter;

import Combat_Starter.Enums.ScriptState;
import Combat_Starter.Enums.Target;
import Combat_Starter.Executes.Fighting;
import Combat_Starter.Helpers.CombatStyle;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.api.component.tab.*;
import org.rspeer.runetek.event.listeners.ChatMessageListener;
import org.rspeer.runetek.event.types.ChatMessageEvent;
import org.rspeer.runetek.event.types.ChatMessageType;
import org.rspeer.script.Script;
import org.rspeer.script.ScriptCategory;
import org.rspeer.script.ScriptMeta;
import org.rspeer.ui.Log;

import java.util.Arrays;
import java.util.stream.Stream;

@ScriptMeta(desc = "Kills NPCs around Lumbridge", developer = "Shteve", name = "Combat Starter", category = ScriptCategory.COMBAT, version = 0.1)
public class Main extends Script implements ChatMessageListener {

    private static ScriptState currentState = ScriptState.STARTING;
    private static ScriptState previousState;

    private static Target currentTarget;


    @Override
    public void onStart() {
        Log.fine("Running Combat Starter by Shteve");
        super.onStart();
    }

    @Override
    public int loop() {

        currentState.execute();
        return 150;
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    @Override
    public void notify(ChatMessageEvent chatMessageEvent) {
        String Message = chatMessageEvent.getMessage();
        if (chatMessageEvent.getType() == ChatMessageType.SERVER){
            if (Message.contains("Congratulations, ")){
               onLevelUpEvent();
            }
        }
    }

    //region Getters & Setters
    public static void updateScriptState(ScriptState inState){
        if (inState == null){
            Log.severe("Error: updateScriptState was passed a null ref");
        }else {
            previousState = currentState;
            if (inState == currentState)
                Log.severe("Error: New script state same as previous.");
            else
                currentState = inState;
        }
    }

    public static ScriptState getPreviousScriptState(){
        return previousState;
    }

    public static Target getCurrentTarget(){
        return currentTarget;
    }

    private static void updateTarget(Target inTarget){
        currentTarget = inTarget;
    }
    //endregion


    //region Public Methods
    /**
     * Checks if the only items the player has is a sword and shield.
     * Checks both inventory and equipped gear.
     * @return Returns true if only got sword and shield otherwise returns false.
     */
    public static boolean onlyHasEquipment(){
        //Items from both invent and equipped
        Item[] items = Stream.concat(
                Arrays.stream(Inventory.getItems()),
                Arrays.stream(Equipment.getItems()))
                .toArray(Item[]::new);

        if (items.length != 2)
            return false;

        if (items[0].getName().contains("sword") && items[1].getName().contains("shield"))
            return true;
        if (items[1].getName().contains("sword") && items[0].getName().contains("shield"))
            return true;
        return false;
    }

    /**
     * Checks for the best attack style and best target and updates if necessary.
     */
    public static void onLevelUpEvent(){
        //TODO Potentially rename method.
        //Update combat style to give even levels
        Combat.AttackStyle currentAttackStyle = Combat.getAttackStyle();
        Combat.AttackStyle bestAttackStyle = Fighting.getBestAttackStyle();
        if (bestAttackStyle != currentAttackStyle)
            CombatStyle.setAttackStyle(bestAttackStyle);

        //Update target to give best xp based on levels
        Target bestTarget = Target.getBestTarget();
        if (bestTarget != currentTarget){
            //Walk to the new training area unless just starting the script
            //If just starting the script we need to do more checks before walking
            if (currentTarget != null)
                updateScriptState(ScriptState.WALKING);

            Log.info("New target NPC found.");
            updateTarget(bestTarget);
        }
    }
    //endregion
}