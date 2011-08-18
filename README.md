Simple Server Reversi
=====================

This project mainly serves as a demonstration of using [Narya] and [Vilya] to
create a completely standalone client and server for a [Game Gardens]-like
game.

It omits any sort of lobby, in the name of simplicity, but integrating a lobby
is not tremendously difficult, and simple code to do so exists in the [Micasa]
package in Vilya.

Building and Running
--------------------

Build and run the demo using Ant:

    # run each of the following commands in a separate shell
    % ant server
    % ant -Dusername=Ren client
    % ant -Dusername=Stimpy client

Distribution
------------

This demo is released under the BSD license. See the LICENSE file for details.

Contact
-------

Questions, comments, and other communications can be directed to the [Three
Rings Libraries] Google Group.

[Game Gardens]: http://www.gamegardens.com/
[Micasa]: http://threerings.github.com/vilya/apidocs/com/threerings/micasa/lobby/package-summary.html
[Narya]: http://github.com/threerings/narya/
[Vilya]: http://github.com/threerings/vilya/
[Three Rings Libraries]: http://groups.google.com/group/ooo-libs
