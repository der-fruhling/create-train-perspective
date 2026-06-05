Changelog:

- **Fixed:** The new option added in Create v0.5.1.g, "Rotate when Seated," is now disabled automagically by this mod as it conflicts very intensely with it.
  - Fixes the issue where the camera is turning double the distance it should.
- **Fixed:** Description of debug features.
- **Fixed:** Text of some options to more accurately reflect the states they can be in.
- **Fixed:** Perspective weirdness when the camera is rolling.
  - Turns out, I'm an idiot and had the correct solution to this problem, which I then removed in [this commit](https://github.com/der-fruhling-entertainment/create-train-perspective/commit/089cb9a38c79a01e9fd94c8df794a433d7c49b70) and spent weeks scratching my head, trying to figure out why it wasn't working properly.
  - Sorry for the terrible-ness, it's pretty good now, and I have no more reason to break it. (yet)
- **Added:** Per-player mod kill-switch.
  - Add player UUIDs to the new "Blocked Players" list in the config to disable this mod for them specifically.
  - Maybe useful with mods like Figura, where a player's avatar _might_ break this mod.

[View full change log.](https://github.com/der-fruhling/create-train-perspective/compare/v1.0.0...v1.1.0)

---

Issues?
Feature Requests?
[View the issue tracker!](https://github.com/der-fruhling-entertainment/create-train-perspective/issues)

Questions?
[Join the Discord!](https://discord.gg/AyM66DhPKr)
Or,
[discuss on GitHub!](https://github.com/der-fruhling-entertainment/create-train-perspective/discussions)
