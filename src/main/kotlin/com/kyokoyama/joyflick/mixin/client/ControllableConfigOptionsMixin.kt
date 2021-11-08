package com.kyokoyama.joyflick.mixin.client

import com.mrcrayfish.controllable.Config
import com.mrcrayfish.controllable.client.ActionVisibility
import com.mrcrayfish.controllable.client.ControllerIcons
import com.mrcrayfish.controllable.client.CursorType
import com.mrcrayfish.controllable.client.Thumbstick
import net.minecraft.client.resources.I18n
import net.minecraftforge.common.ForgeConfigSpec
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue
import net.minecraftforge.common.ForgeConfigSpec.EnumValue
import org.spongepowered.asm.mixin.Final
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(Config.Client.Options::class)
class ControllableConfigOptionsMixin {
    @Shadow
    @Final
    lateinit var forceFeedback: ForgeConfigSpec.BooleanValue

    @Shadow
    @Final
    lateinit var autoSelect: ForgeConfigSpec.BooleanValue

    @Shadow
    @Final
    lateinit var renderMiniPlayer: ForgeConfigSpec.BooleanValue

    @Shadow
    @Final
    lateinit var virtualMouse: ForgeConfigSpec.BooleanValue

    @Shadow
    @Final
    lateinit var consoleHotbar: ForgeConfigSpec.BooleanValue

    @Shadow
    @Final
    lateinit var cursorType: EnumValue<CursorType>

    @Shadow
    @Final
    lateinit var controllerIcons: EnumValue<ControllerIcons>

    @Shadow
    @Final
    lateinit var invertLook: ForgeConfigSpec.BooleanValue

    @Shadow
    @Final
    lateinit var deadZone: DoubleValue

    @Shadow
    @Final
    lateinit var rotationSpeed: DoubleValue

    @Shadow
    @Final
    lateinit var mouseSpeed: DoubleValue

    @Shadow
    @Final
    lateinit var showActions: EnumValue<ActionVisibility>

    @Shadow
    @Final
    lateinit var quickCraft: ForgeConfigSpec.BooleanValue

    @Shadow
    @Final
    lateinit var uiSounds: ForgeConfigSpec.BooleanValue

    @Shadow
    @Final
    lateinit var radialThumbstick: EnumValue<Thumbstick>


    @Inject(method = ["<init>"], at = [At("HEAD")], cancellable = true, remap = false)
    fun constructor(builder: ForgeConfigSpec.Builder, callback: CallbackInfo) {
        builder.comment("ゲーム関連のオプションです。コンフィグファイルではなく、ゲーム内でこれらの項目を変更できます。")

        this.forceFeedback = builder.comment(I18n.format("controllable.options.forceFeedback.desc"))
            .define("forceFeedback", true)
        this.autoSelect =
            builder.comment(I18n.format("controllable.options.autoSelect.desc"))
                .define("autoSelect", true)
        this.renderMiniPlayer =
            builder.comment(I18n.format("controllable.options.renderMiniPlayer.desc"))
                .define("renderMiniPlayer", true)
        this.virtualMouse =
            builder.comment(I18n.format("controllable.options.virtualMouse.desc"))
                .define("virtualMouse", true)
        this.consoleHotbar =
            builder.comment(I18n.format("controllable.options.consoleHotbar.desc"))
                .define("consoleHotbar", false)
        this.cursorType =
            builder.comment(I18n.format("controllable.options.cursorType.desc"))
                .defineEnum("cursorType", CursorType.LIGHT)
        this.controllerIcons = builder.comment(I18n.format("controllable.options.controllerIcons.desc"))
            .defineEnum("controllerIcons", ControllerIcons.SWITCH_CONTROLLER)
        this.invertLook = builder.comment(I18n.format("controllable.options.invertLook.desc"))
            .define("invertLook", false)
        this.deadZone =
            builder.comment(I18n.format("controllable.options.deadZone.desc"))
                .defineInRange("deadZone", 0.15, 0.0, 1.0)
        this.rotationSpeed = builder.comment(I18n.format("controllable.options.rotationSpeed.desc"))
            .defineInRange("rotationSpeed", 25.0, 0.0, 100.0)
        this.mouseSpeed = builder.comment("The speed which the cursor or virtual mouse moves around the screen")
            .defineInRange("mouseSpeed", 15.0, 0.0, 50.0)
        this.showActions =
            builder.comment(I18n.format("controllable.options.mouseSpeed.desc"))
                .defineEnum("showActions", ActionVisibility.MINIMAL)
        this.quickCraft =
            builder.comment(I18n.format("controllable.options.quickCraft.desc"))
                .define("quickCraft", true)
        this.uiSounds =
            builder.comment(I18n.format("controllable.options.uiSounds.desc"))
                .translation("controllable.config.uiSounds").define("uiSounds", true)
        this.radialThumbstick = builder.comment(I18n.format("controllable.options.radialThumbstick.desc"))
            .translation("controllable.config.radialThumbstick").defineEnum("radialThumbstick", Thumbstick.RIGHT)

        builder.pop()

        callback.cancel()
    }
}