module Sdl {
  requires transitive java.instrument;
  requires org.javassist;
  requires org.objectweb.asm;
  requires org.objectweb.asm.tree;
  requires org.objectweb.asm.commons;

  exports sdl;
}
