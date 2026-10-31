# Security

Minified handles Microsoft / Minecraft access tokens (minified-auth), downloads and runs code (libraries, mods, Java runtimes) and writes to world saves.
If you find something that could leak tokens, run code that shouldn't run or let someone mess with files they shouldn't touch, please don't open a public issue for it.

## Reporting

Report it privately here: **[Report a vulnerability](https://github.com/dervarex/minified/security/advisories/new)**

Only the maintainers can see it. Helpful to include:

- what the problem is and what someone could do with it
- how to reproduce it, a small code example is perfect
- the minified version(s) and module(s) affected

Not sure if it's actually a security problem? Report it privately anyway, worst case it gets moved to a normal issue.

## What happens then

- you'll usually get a reply within a few days, but since this is just a Hobby Project, it might take longer.
- once it's confirmed, it gets fixed in a new release as fast as possible, we'll credit you if you want to

## Supported versions

Only the latest release gets security fixes. Releases up to v3.2.3 are only on JitPack and won't get any, please update to the Maven Central releases (`io.github.dervarex.minified`).

## Not a minified problem

- someone getting your access token because your own launcher logs it or stores it in plain text. minified gives you the token, keeping it safe is your launcher's job (though if minified itself logs it somewhere, that is a minified problem, report it!)
- vulnerabilities in Minecraft itself, mod loaders or mods. Report those to Mojang or the project that's affected
