---
title: Mixins

---

Mixins are a powerful tool used to modify the compiled bytecode of Minecraft as it loads.
This allows modders to inject custom code, change existing game logic, read private variables or completely overwrite existing methods.

### How and why we use Mixins
Because Minecraft's source code is "read-only" and compiled, you cannot just rewrite a core game class in your mod.
Instead, Mixins use Java annotations to tell the Mixin processor exactly where, when, and how to change the game's compiled bytecode.
**Common Types of Mixins:**
<div class="fields-details" open>
  <div class="fields-grid">

    <div class="field-card">
      <div class="field-header">
        <code>@Inject</code>
      </div>
      <p>This is the most common method. It inserts your custom code right at the head, tail, or before/after specific method calls.</p>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>@ModifyVariable</code> / <code>@ModifyArg</code>
      </div>
      <p>Used to intercept and change the value of variables or method arguments before they are processed by vanilla code.</p>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>@Redirect</code>
      </div>
      <p>Replaces a specific method call or field access in the vanilla game with your own custom method call.</p>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>@Accessor</code> / <code>@Invoker</code>
      </div>
      <p>Used to make private or protected fields and methods from Minecraft's source code accessible and usable in your mod's codebase.</p>
    </div>
  </div>
</div>

### Example
```java
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @Inject(method = "jump", at = @At("HEAD"))
    private void onJump(CallbackInfo ci) {
        // custom code here, for example printing
        // a message when the player jumps
    }
}
```