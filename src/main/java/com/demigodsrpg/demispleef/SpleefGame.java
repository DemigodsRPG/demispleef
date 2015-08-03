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
            session.endSession();
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
            if (DefaultStage.PLAY.equals(session.getStage()) && event.getTo().distance(getWarmupSpawn(session)) > 30) {
                // Update the stage
                session.updateStage(DefaultStage.END, true);
            }
        }
    }

    //List of blocks that can be broken
    private final static List<Material> breakable = Lists.newArrayList(Material.SNOW_BLOCK, Material.WOOL, Material.CLAY, Material.DIRT, Material.TNT);

    //Cancel breaking any other block than the ones specified above
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBreak(BlockBreakEvent e){
        Block b = e.getBlock();
        Optional<Session> opSession = checkPlayer(e.getPlayer());
        if (opSession.isPresent()) {
            Session session = opSession.get();

            if(!session.getStage().equals(DefaultStage.PLAY) || !breakable.contains(b.getType())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)


    // -- PLAYER DEATH -- //

    @Override
    public void onDeath(Session session, EntityDamageEvent entityDamageEvent)
    {
        Player damaged = (Player) entityDamageEvent.getEntity();

        //TODO Spectator mode

        //TODO check if the player is the last one in the game, then start the end timer
    }

    // -- META DATA -- //

    @Override
    public Location getWarmupSpawn(Session session) {
        Optional<Location> spawn = Optional.ofNullable(LocationUtil.locationFromString(session.getId(), warmupSpawn));
        if (spawn.isPresent()) {
            return spawn.get();
        }
        return Bukkit.getWorld(session.getId()).getSpawnLocation();
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

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWin(PlayerWinMinigameEvent event) {

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLose(PlayerLoseMinigameEvent event) {

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTie(PlayerTieMinigameEvent event) {

    }

    // -- START & STOP -- //

    @Override
    public void onServerStart() {

    }

    @Override
    public void onServerStop() {
        Demigames.getSessionRegistry().fromGame(this).forEach(com.demigodsrpg.demigames.session.Session::endSession);
    }

    // -- PLAYER JOIN/QUIT -- //

    @Override
    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinMinigameEvent event) {
        if (event.getGame().isPresent() && event.getGame().get().equals(this)) {
            Optional<Session> opSession = checkPlayer(event.getPlayer());

            if (opSession.isPresent()) {
                // TODO Only has warmup join atm
                event.getPlayer().teleport(getWarmupSpawn(opSession.get()));

                // Add the spleef kit to the player if it is existing
                Optional<MutableKit> kit = Demigames.getKitRegistry().fromKey("spleef");
                if (kit.isPresent()) {
                    ImmutableKit.of(kit.get()).apply(event.getPlayer(), true);
                } else {
                    Kit.EMPTY.apply(event.getPlayer(), true);
                }
            }
        }
    }

    @Override
    @EventHandler(priority = EventPriority.MONITOR)
    public void onLeave(PlayerQuitMinigameEvent event) {
        if (event.getGame().isPresent() && event.getGame().get().equals(this)) {
            Kit.EMPTY.apply(event.getPlayer(), true);

            Optional<Session> opSession = checkPlayer(event.getPlayer());
            if (opSession.isPresent()) {
                Session session = opSession.get();
                if (session.getProfiles().size() < 1) {
                    session.endSession();
                }
            }
        }
    }

    // -- FAKE DEATH -- //

    @Override
    public void onDeath(Session session, EntityDamageEvent event) {
        Player player = (Player) event.getEntity();
        player.sendMessage("HAHAHAH you died.");
    }
}
