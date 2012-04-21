(ns horsetail.core
  (:require [cloft.cloft :as c])
  ;(:require [clojure.core.match :as m])
  (:require [swank.swank])
  (:require [clojure.string :as s])
  (comment (:import [org.bukkit.command CommandExecuter CommandSender Command]))
  (:import [org.bukkit Bukkit Material])
  (:import [org.bukkit.entity Animals Arrow Blaze Boat CaveSpider Chicken
            ComplexEntityPart ComplexLivingEntity Cow Creature Creeper Egg
            EnderCrystal EnderDragon EnderDragonPart Enderman EnderPearl
            EnderSignal ExperienceOrb Explosive FallingSand Fireball Fish
            Flying Ghast Giant HumanEntity Item LightningStrike LivingEntity
            MagmaCube Minecart Monster MushroomCow NPC Painting Pig PigZombie
            Player PoweredMinecart Projectile Sheep Silverfish Skeleton Slime
            SmallFireball Snowball Snowman Spider Squid StorageMinecart
            ThrownPotion TNTPrimed Vehicle Villager WaterMob Weather Wolf
            Zombie])
  (:import [org.bukkit.event.entity EntityDamageByEntityEvent
            EntityDamageEvent$DamageCause])
  (:import [org.bukkit.potion Potion PotionEffect PotionEffectType])
  (:import [org.bukkit.inventory ItemStack]))

(defn player-super-jump [evt player]
  (let [name (.getDisplayName player)]
    (when (= (.getType (.getItemInHand player)) Material/FEATHER)
        (let [amount (.getAmount (.getItemInHand player))
              x (if (.isSprinting player) (* amount 2) amount)
              x2 (/ (java.lang.Math/log x) 2) ]
          (c/consume-itemstack (.getInventory player) Material/FEATHER)
          (.setVelocity
            player
            (.add (org.bukkit.util.Vector. 0.0 x2 0.0) (.getVelocity player)))))))

(defn player-interact-event [evt]
  (let [player (.getPlayer evt)]
    (cond
      (and
        (= (.. player (getItemInHand) (getType)) Material/GLASS_BOTTLE)
        (or
          (= (.getAction evt) org.bukkit.event.block.Action/RIGHT_CLICK_AIR)
          (= (.getAction evt) org.bukkit.event.block.Action/RIGHT_CLICK_BLOCK)))
      (.setItemInHand player (.toItemStack (Potion. (rand-nth c/potion-types))  (rand-nth [1 1 2 3 5])))
      (and
        (= (.. player (getItemInHand) (getType)) Material/GOLD_SWORD)
        (= (.getHealth player) (.getMaxHealth player))
        (or
          (= (.getAction evt) org.bukkit.event.block.Action/LEFT_CLICK_AIR)
          (= (.getAction evt) org.bukkit.event.block.Action/LEFT_CLICK_BLOCK)))
      (.throwSnowball player)
      (and
        (= (.. evt (getMaterial)) Material/MILK_BUCKET)
        (or
          (= (.getAction evt) org.bukkit.event.block.Action/RIGHT_CLICK_AIR)
          (= (.getAction evt) org.bukkit.event.block.Action/RIGHT_CLICK_BLOCK)))
      (do
        (.damage player 8)
        (.sendMessage player "you drunk milk"))
      (and
        (= (.. evt (getMaterial)) Material/FEATHER)
        (or
          (= (.getAction evt) org.bukkit.event.block.Action/RIGHT_CLICK_AIR)
          (= (.getAction evt) org.bukkit.event.block.Action/RIGHT_CLICK_BLOCK)))
      (player-super-jump evt player))))

(defn player-interact-entity-event [evt]
  (let [target (.getRightClicked evt)]
    (letfn [(d [n]
              (.dropItem (.getWorld target)
                         (.getLocation target)
                         (ItemStack. n 1)))]
      (cond
        (and (= (.getType (.getItemInHand (.getPlayer evt))) Material/COAL)
             (instance? PoweredMinecart target))
        (do
          (.setMaxSpeed target 5.0)
          (let [v (.getVelocity target)
                x (.getX v)
                z (.getY v)
                r2 (max (+ (* x x) (* z z)) 0.1)
                new-x (* 2 (/ x r2))
                new-z (* 2 (/ z r2))]
            (future-call #(do
                            (Thread/sleep 100)
                            (.setVelocity target (org.bukkit.util.Vector. new-x (.getY v) new-z))))))
        (and (instance? PigZombie target)
             (= (.getTypeId (.getItemInHand (.getPlayer evt))) 296))
        (do
          (c/swap-entity target Pig)
          (c/consume-item (.getPlayer evt)))
        (and (instance? Pig target)
             (= (.getTypeId (.getItemInHand (.getPlayer evt))) 367))
        (do
          (c/swap-entity target PigZombie)
          (c/consume-item (.getPlayer evt)))
        (instance? Chicken target) (d 66)
        (instance? Cow target) (d 263)
        (instance? Villager target) (d 92)
        (instance? Creeper target) (d 289)
        (and (instance? Zombie target) (not (instance? PigZombie target))) (d 367)
        (instance? Skeleton target) (d 262)
        (instance? Spider target) (d 287)
        (instance? Squid target)
        (let [player (.getPlayer evt)]
          (.chat player "ikakawaiidesu")
          (.setFoodLevel player 0))
        (instance? Player target) (touch-player target)))))

(defonce swank* nil)
(defn on-enable [plugin]
  (when (nil? swank*)
    (def swank* (swank.swank/start-repl 4009))))
