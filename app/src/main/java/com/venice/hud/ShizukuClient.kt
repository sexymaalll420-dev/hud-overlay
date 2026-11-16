          package com.venice.hud

          import android.content.ComponentName
          import android.content.Context
          import android.content.ServiceConnection
          import android.content.pm.PackageManager
          import android.os.IBinder
          import rikka.shizuku.Shizuku

          object ShizukuClient {

              @Volatile var inject: IInject? = null
              val ready: Boolean get() = inject != null

              private var conn: ServiceConnection? = null
              private var args: Shizuku.UserServiceArgs? = null

              fun usable(ctx: Context): Boolean {
                  return try {
                      Shizuku.pingBinder() &&
                          !Shizuku.isPreV11() &&
                          Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
                  } catch (t: Throwable) { false }
              }

              fun connect(ctx: Context) {
                  if (inject != null) return
                  if (!usable(ctx)) return

                  val a = Shizuku.UserServiceArgs(
                      ComponentName(ctx.packageName, InjectService::class.java.name)
                  ).daemon(false).processNameSuffix("inject").debuggable(false).version(4)

                  val c = object : ServiceConnection {
                      override fun onServiceConnected(n: ComponentName?, b: IBinder?) {
                          inject = try { IInject.Stub.asInterface(b) } catch (t: Throwable) { null }
                      }
                      override fun onServiceDisconnected(n: ComponentName?) { inject = null }
                  }
                  args = a
                  conn = c
                  try { Shizuku.bindUserService(a, c) } catch (t: Throwable) { inject = null }
              }

              fun disconnect() {
                  val c = conn ?: return
                  val a = args
                  try { if (a != null) Shizuku.unbindUserService(a, c, true) } catch (t: Throwable) {}
                  conn = null
                  inject = null
              }
          }