package com.rtcvoice;

import androidx.annotation.NonNull;

import com.fpnn.sdk.FunctionalAnswerCallback;
import com.fpnn.sdk.proto.Answer;
import com.fpnn.sdk.proto.Quest;
import com.livedata.rtc.RTCEngine;
import com.rtcvoice.RTMStruct.RTMAnswer;
import com.rtcvoice.RTMStruct.RoomInfo;
import com.rtcvoice.UserInterface.IRTMCallback;
import com.rtcvoice.UserInterface.IRTMEmptyCallback;

import java.util.HashSet;

public class RTMRTC extends RTMChat{
    /**
     *管理员权限操作
     * @param roomId 房间id
     * @param uids
     * @param command
     * 0 赋予管理员权
     * 1 剥夺管理员权限
     * 2 禁止发送音频数据
     * 3 允许发送音频数据
     * 4 禁止发送视频数据
     * 5 允许发送视频数据
     * 6 关闭他人麦克风
     * 7 关闭他人摄像头
     * @return
     */
    public void adminCommand (IRTMEmptyCallback callback, long roomId, HashSet<Long> uids,  int command){
        Quest quest = new Quest("adminCommand");
        quest.param("rid", roomId);
        quest.param("uids", uids);
        quest.param("command", command);
        sendQuestEmptyCallback(callback, quest);
    }


    /**
     * 打开麦克风(音频模式进入房间初始默认关闭  视频模式进入房间默认开启)
     */
    public void openMic(){
        RTCEngine.canSpeak(true);
    }

    /**
     * 关闭麦克风
     */
    public void closeMic(){
        RTCEngine.canSpeak(false);
    }


    /**
     * 设置麦克风增益等级(声音自动增益 取值 范围0-10)
     */
    public void setMicphoneLevel(int level){
        if (level <= 0 || level >= 10)
            return;
        RTCEngine.setMicphoneGain(level);
    }


    /**
     * 创建RTC房间
     * @roomId 房间id
     * @param callback 回调
     */
    public void createRTCRoom(@NonNull final IRTMCallback<RoomInfo> callback, final long roomId) {
        final RoomInfo ret = new RoomInfo();
        ret.roomId = roomId;
        ret.roomTyppe = 1;
        Quest quest = new Quest("createRTCRoom");
        quest.param("rid", roomId);
        quest.param("type", 1);
        quest.param("enableRecord", 0);
        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                if (errorCode == okRet) {
                    String roomToken = rtmUtils.wantString(answer,"token");
                    enterRTCRoomReal(callback,roomId, roomToken,ret);
                }
                else {
                    ret.errorCode = errorCode;
                    ret.errorMsg = answer.getErrorMessage();
                    callback.onResult(ret, genRTMAnswer(answer, errorCode));
                }
            }
        });
    }

    /**
     * 进入RTC房间
     * @param callback 回调
     * @param roomId   房间id
     */
    public void enterRTCRoom(@NonNull final IRTMCallback<RoomInfo> callback, final long roomId) {
        super.enterRTCRoom(callback,roomId);
    }

    /**
     * 邀请用户加入RTC房间(非强制，需要对端确认)(发送成功仅代表收到该请求，至于用户最终是否进入房间结果未知)
     * @param callback 回调
     * @param roomId   房间id
     * @param uids     需要邀请的用户列表
     */
    public void inviteUserIntoRTCRoom(IRTMEmptyCallback callback, long roomId, HashSet<Long> uids){
        Quest quest = new Quest("inviteUserIntoRTCRoom");
        quest.param("rid",roomId);
        quest.param("uids",uids);

        sendQuestEmptyCallback(callback, quest);
    }

    /**
     * 设置目前活跃的房间(仅对语音房间有效)
     * @param roomId
     */
    public RTMAnswer setActivityRoom(long roomId){
        String msg = RTCEngine.setActivityRoom(roomId);
        if (msg.isEmpty()){
            return genRTMAnswer(okRet);
        }
        else
            return genRTMAnswer(voiceError,msg);
    }

    /**
     * 切换扬声器听筒(耳机状态下不操作)(默认扬声器)
     * @param usespeaker true-使用扬声器 false-使用听筒
     */
    public void switchOutput(boolean usespeaker){
        if (isHeadsetOn())
            return;
        RTCEngine.switchVoiceOutput(usespeaker);
//        if (usespeaker)
//            mAudioManager.setSpeakerphoneOn(true);
//        else
//            mAudioManager.setSpeakerphoneOn(false);
    }

    /**
     * 设置语音开关(开启语音功能或者关闭语音功能(如果为语音功能关闭则麦克风自动关闭)
     * @param status
     */
    public RTMAnswer setVoiceStat(boolean status){
        String msg = RTCEngine.setVoiceStat(status);
        if (msg.isEmpty())
            return genRTMAnswer(okRet);
        else
            return genRTMAnswer(voiceError,msg);
    }

    /**离开RTC房间
     * @param roomId   房间id
     */
    public void leaveRTCRoom(final long roomId){
        Quest quest = new Quest("exitRTCRoom");
        quest.param("rid",roomId);
        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                RTCEngine.leaveRTCRoom(roomId);
            }
        });
    }

    /**
     * 屏蔽房间某些人的语音
     * @param callback 回调
     * @param roomId   房间id
     * @param uids     屏蔽语音的用户列表
     */
    public void blockUserInVoiceRoom(IRTMEmptyCallback callback, long roomId, HashSet<Long> uids){
        Quest quest = new Quest("blockUserVoiceInRTCRoom");
        quest.param("rid",roomId);
        quest.param("uids",uids);
        sendQuestEmptyCallback(callback, quest);
    }

    /**
     * 解除屏蔽房间某些人的语音
     * @param callback 回调
     * @param roomId   房间id
     * @param uids     解除屏蔽语音的用户列表
     */
    public void unblockUserInVoiceRoom(IRTMEmptyCallback callback, long roomId, HashSet<Long> uids){
        Quest quest = new Quest("unblockUserVoiceInRTCRoom");
        quest.param("rid",roomId);
        quest.param("uids",uids);
        sendQuestEmptyCallback(callback, quest);
    }


    /**
     * 获取语RTC房间成员列表
     * @param callback 回调<RoomInfo>
     */
    public void getRTCRoomMembers(@NonNull final IRTMCallback<RoomInfo> callback, long roomId) {
        Quest quest = new Quest("getRTCRoomMembers");
        quest.param("rid", roomId);

        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                RoomInfo tt = new RoomInfo();
                if (errorCode == okRet) {
                    tt.uids = rtmUtils.wantLongHashSet(answer, "uids");
                    tt.managers = rtmUtils.wantLongHashSet(answer, "administrators");
                    tt.owner = rtmUtils.wantInt(answer,"owner");
                }
                callback.onResult(tt, genRTMAnswer(answer, errorCode));
            }
        });
    }

    /**
     * 获取RTC房间成员个数
     * @param callback 回调
     */
    public void getRTCRoomMemberCount(@NonNull final IRTMCallback<Integer> callback, long roomId) {
        Quest quest = new Quest("getRTCRoomMemberCount");
        quest.param("rid", roomId);

        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                int count = 0;
                if (errorCode == okRet)
                    count = rtmUtils.wantInt(answer,"count");
                callback.onResult(count, genRTMAnswer(answer, errorCode));
            }
        });
    }
}


