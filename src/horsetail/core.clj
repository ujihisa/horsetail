(ns horsetail.core
  (:require [cloft.cloft :as c])
  (:require [swank.swank])
  (:require [clojure.set :as s])
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
  (:import [org.bukkit.inventory ItemStack])
  (:import [org.bukkit.event.entity EntityDamageByEntityEvent
            EntityDamageEvent$DamageCause]))

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

(defonce swank* nil)
(defn on-enable [plugin]
  (when (nil? swank*)
    (def swank* (swank.swank/start-repl 4009))))
