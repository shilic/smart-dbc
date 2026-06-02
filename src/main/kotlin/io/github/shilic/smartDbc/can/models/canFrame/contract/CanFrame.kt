package io.github.shilic.smartDbc.can.models.canFrame.contract

import io.github.shilic.numberUtils.toHexStr

/**
 * CAN帧接口
 *
 * @property msgId 报文ID
 * @property data 报文数据
 * @property sendType 发送帧类型, 只在此帧为发送帧时有意义。;
 * @property remoteFlag 远程帧标志, 默认0 数据帧;
 * @property externFlag 扩展帧标志
 * @property fdFlag CanFd 标志位
 * @property dataLen 数据长度, 自动赋值为 [CanFrame.data] 的长度
 */
interface CanFrame {
    /** 报文ID */
    val msgId: Int
    /** 数据 */
    val data: ByteArray
    /**SendType 发送帧类型。默认0
     *
     * =0 时为 正常发送（发送失败会自动重发，重发最长时间为 1.5-3 秒）；
     *
     * =1 时为 单次发送（只发送一次，不自动重发）；
     *
     * =2 时为 自发自收（自测试模式，用于测试 CAN 卡是否损坏）；
     *
     * =3 时为 单次自发自收（单次自测试模式，只发送一次）。
     *
     * 只在此帧为发送帧时有意义。
     * */
    val sendType: Int
    /** RemoteFlag 是否是远程帧。默认0
     *
     * =0 时为 数据帧（数据帧）；
     *
     * =1 时为 远程帧（远程帧）。
     * */
    val remoteFlag: Int
    /**ExternFlag 是否是扩展帧。默认1
     *
     * =0 时为 标准帧（标准帧）；
     *
     * =1 时为 扩展帧（扩展帧）。
     * */
    val externFlag: Int
    /** 是否启用 canFd
     *
     * 0表示关闭. 也就是普通CAN总线;
     *
     * 1表示启用;
     * */
    val fdFlag: Int
    /** 数据长度, 自动赋值为 [CanFrame.data] 的长度*/
    val dataLen :Int get() = data.size
    /**  帧数据展示 */
    val display: String get() = "CanFrame: [msgId: ${msgId.toHexStr()}, data:${data.toHexStr()}]"
}