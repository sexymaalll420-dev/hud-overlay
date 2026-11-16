          package com.venice.hud;

          interface IInject {
              boolean swipe(float x1, float y1, float x2, float y2, int ms);
              boolean tap(float x, float y);
              boolean snapHold(float cx, float cy, float tx, float ty, float fx, float fy, int ms);
              boolean fireDown(float x, float y);
              boolean fireUp(float x, float y);
              boolean ping();
          }