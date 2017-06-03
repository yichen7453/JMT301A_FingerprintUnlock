/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: D:\\Android Project\\JMT301A_FingerprintUnlock\\src\\com\\android\\internal\\policy\\IFingerprintLockCallback.aidl
 */
package com.android.internal.policy;
/** {@hide} */
public interface IFingerprintLockCallback extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.android.internal.policy.IFingerprintLockCallback
{
private static final java.lang.String DESCRIPTOR = "com.android.internal.policy.IFingerprintLockCallback";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.android.internal.policy.IFingerprintLockCallback interface,
 * generating a proxy if needed.
 */
public static com.android.internal.policy.IFingerprintLockCallback asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.android.internal.policy.IFingerprintLockCallback))) {
return ((com.android.internal.policy.IFingerprintLockCallback)iin);
}
return new com.android.internal.policy.IFingerprintLockCallback.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_unlock:
{
data.enforceInterface(DESCRIPTOR);
this.unlock();
return true;
}
case TRANSACTION_cancel:
{
data.enforceInterface(DESCRIPTOR);
this.cancel();
return true;
}
case TRANSACTION_reportFailedAttempt:
{
data.enforceInterface(DESCRIPTOR);
this.reportFailedAttempt();
return true;
}
case TRANSACTION_pokeWakelock:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
this.pokeWakelock(_arg0);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.android.internal.policy.IFingerprintLockCallback
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public void unlock() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_unlock, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
@Override public void cancel() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_cancel, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
@Override public void reportFailedAttempt() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_reportFailedAttempt, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
@Override public void pokeWakelock(int millis) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(millis);
mRemote.transact(Stub.TRANSACTION_pokeWakelock, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
}
static final int TRANSACTION_unlock = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_cancel = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_reportFailedAttempt = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_pokeWakelock = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
}
public void unlock() throws android.os.RemoteException;
public void cancel() throws android.os.RemoteException;
public void reportFailedAttempt() throws android.os.RemoteException;
public void pokeWakelock(int millis) throws android.os.RemoteException;
}
