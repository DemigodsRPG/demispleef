/*
 * Copyright (c) 2015 Demigods RPG
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.demigodsrpg.demispleef;

import com.demigodsrpg.demigames.game.Game;
import com.demigodsrpg.demigames.game.mixin.ErrorTimerMixin;
import com.demigodsrpg.demigames.game.mixin.FakeDeathMixin;
import com.demigodsrpg.demigames.game.mixin.SetupNoTeamsMixin;
import com.demigodsrpg.demigames.game.mixin.WarmupLobbyMixin;
import com.demigodsrpg.demigames.impl.Demigames;
import com.demigodsrpg.demigames.impl.util.LocationUtil;
import com.demigodsrpg.demigames.kit.Kit;
import com.demigodsrpg.demigames.kit.MutableKit;
import com.demigodsrpg.demigames.session.Session;
import com.demigodsrpg.demigames.stage.DefaultStage;
import com.demigodsrpg.demigames.stage.StageHandler;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Optional;

public class SpleefGame implements Game, WarmupLobbyMixin, SetupNoTeamsMixin, ErrorTimerMixin, FakeDeathMixin {
    // -- SETTINGS -- //

    @Override
    public boolean canPlace() {
        return false;
    }

    @Override
    public boolean canBreak() {
        return true;
    }

    @Override
    public boolean canDrop() {
        return false;
    }

    @Override
    public boolean canLateJoin() {
        return false;
    }

    @Override
    public boolean hasSpectateChat() {
        return false;
    }

    @Override
    public int getMinimumPlayers() {
        return 3;
    }

    @Override
    public int getNumberOfTeams() {
        return 0;
    }

    // -- LOCATIONS -- //

    private Location warmupSpawn;

    @Override
    public void setupLocations(Session session) {
        // Get the world
        World world = session.getWorld().get();

        // Get the warmup spawn
        warmupSpawn = LocationUtil.locationFromString(session, getConfig().getString("loc.spawn",
                LocationUtil.stringFromLocation(world.getSpawnLocation(), false)));
    }

    // -- STAGES -- //

    @StageHandler(stage = DefaultStage.BEGIN)
    public void roundBegin(Session session) {

        // Update the stage
        session.updateStage(DefaultStage.PLAY, true);
    }

    @StageHandler(stage = DefaultStage.PLAY)
    public void roundPlay(Session session) {
        // TODO
    }

    @StageHandler(stage = DefaultStage.END)
    public void roundEnd(Session session) {

        // Update the stage
        session.updateStage(DefaultStage.COOLDOWN, true);
    }

    @StageHandler(stage = DefaultStage.COOLDOWN)
    public void roundCooldown(Session session) {

        // Update the stage
        if (session.getCurrentRound() == getTotalRounds()) {
            session.endSession(false);
        } else {
            session.updateStage(DefaultStage.RESET, true);
        }
    }

    @StageHandler(stage = DefaultStage.RESET)
    public void roundReset(Session session) {

        // Update the stage
        session.updateStage(DefaultStage.SETUP, true);
    }

    // -- LISTENERS -- //

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Optional<Session> opSession = checkPlayer(event.getPlayer());
        if (opSession.isPresent()) {
            Session session = opSession.get();
            if (DefaultStage.PLAY.equals(session.getStage()) && event.getTo().distance(event.getPlayer().getWorld().getSpawnLocation()) > 10) {
                // Update the stage
                session.updateStage(DefaultStage.END, true);
            }
        }
    }

    // -- META DATA -- //

    @Override
    public Location getWarmupSpawn() {
        return warmupSpawn;
    }

    @Override
    public String getName() {
        return "Spleef";
    }


    @Override
    public String getDirectory() {
        return "spleef";
    }

    // -- WIN/LOSE/TIE CONDITIONS -- //

    @Override
    public void onWin(Session session, Player player) {

    }

    @Override
    public void onLose(Session session, Player player) {

    }

    @Override
    public void onTie(Session session, Player player) {

    }

    // -- PLAYER JOIN/QUIT -- //

    @Override
    public void onPlayerJoin(Session session, Player player) {
        //Add the player to the session
        session.addProfile(Demigames.getProfileRegistry().fromPlayer(player));

        //Add the spleef kit to the player if it is existing
        Optional<MutableKit> kit = Demigames.getKitRegistry().fromKey("spleef");
        if(kit.isPresent())
        {
            kit.get().apply(player);
        }
        else
        {
            Kit.EMPTY.apply(player);
        }

    }

    @Override
    public void onPlayerQuit(Session session, Player player) {
        session.removeProfile(player);

        //TODO Teleport player back to the lobby and clear their inventory.
        Kit.EMPTY.apply(player);

    }

    // -- START & STOP -- //

    @Override
    public void onServerStart() {

    }

    @Override
    public void onServerStop() {
        for (Session session : Demigames.getSessionRegistry().fromGame(this)) {
            session.endSession(false);
        }
    }

    @Override
    public void onDeath(Session session, EntityDamageEvent entityDamageEvent) {

    }
}
